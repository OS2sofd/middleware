using DigitalIdentity.SOFD.Model;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text.RegularExpressions;

namespace DigitalIdentity.SOFD
{
    class SOFDService : BaseClass<SOFDService>
    {
        private static readonly string OESTag = "ØS";

        private readonly Uri baseUri;
        private static readonly HttpClient httpClient = new HttpClient();
        public SOFDService(IServiceProvider sp) : base(sp) {
            baseUri = new Uri(appSettings.SOFDSettings.BaseUrl);
            httpClient.DefaultRequestHeaders.Add("ApiKey", appSettings.SOFDSettings.ApiKey);
        }

        public List<OrgUnit> GetOrgUnits()
        {
            logger.LogDebug($"Fetching orgunits from SOFD");
            var response = httpClient.GetAsync(new Uri(baseUri, $"orgUnits?size={appSettings.SOFDSettings.GetOrgUnitsPageSize}"));
            response.Wait();
            response.Result.EnsureSuccessStatusCode();
            var responseString = response.Result.Content.ReadAsStringAsync();
            responseString.Wait();

            var getOrgUnitsDto = JsonConvert.DeserializeObject<GetOrgUnitsDto>(responseString.Result);
            var orgUnits = getOrgUnitsDto.OrgUnits;
            // if we reached max entries on the last page there might be more data in SOFD!
            if (orgUnits.Count == appSettings.SOFDSettings.GetOrgUnitsPageSize)
            {
                throw new Exception("Not all OrgUnits was fetched from SOFD. Increase PageSize");
            }
            ValidateOrgUnits(ref orgUnits);
            var taggedOrgUnits = GetTaggedOrgUnits(orgUnits);
            return taggedOrgUnits;
        }

        private OrgUnit BuildHierarchy(OrgUnit parent, List<OrgUnit> orgUnits)
        {
            foreach (var child in orgUnits.Where(o => o.ParentUuid == parent.Uuid).OrderBy(o => o.Name))
            {
                child.SOFDParent = parent;
                parent.SOFDChildren.Add(BuildHierarchy(child, orgUnits));
            }
            return parent;
        }

        public List<OrgUnit> GetTaggedOrgUnits(List<OrgUnit> orgUnits)
        {
            var taggedOrgUnits = orgUnits.Where(o => o.Tags.Exists(t => t.Tag == SOFDService.OESTag)).ToList();
            return taggedOrgUnits;
        }

        private void ValidateOrgUnits(ref List<OrgUnit> orgUnits)
        {
            var tagRegex = new Regex(@"^[0-9](-\d{2}(-\d{3}(-\d{2}(-\d)?)?)?)?$");
            var root = orgUnits.Where(o => o.ParentUuid == null).Single();
            var taggedOrgUnits = GetTaggedOrgUnits(orgUnits);
            BuildHierarchy(root, orgUnits);
            foreach (var taggedOrgUnit in taggedOrgUnits)
            {
                taggedOrgUnit.OESTagValue = taggedOrgUnit.Tags.Where(t => t.Tag == OESTag).Single().CustomValue;
                if (taggedOrgUnit.OESTagValue == null || !tagRegex.IsMatch(taggedOrgUnit.OESTagValue))
                {
                    taggedOrgUnit.IsValid = false;
                    taggedOrgUnit.ValidationErrors.Add($"Enheden '{taggedOrgUnit.Name}' er opmærket med Adm Org Id '{taggedOrgUnit.OESTagValue}' som ikke lever op til syntakskravet (x-xx-xxx-xx-x) for et Adm Org Id.");
                }
                else if (taggedOrgUnit.OESTagValue.Length == 1) // the tag is a top level org.
                {
                    taggedOrgUnit.IsRoot = true;
                    // Check if any parents are tagged with ØS as well - this would be an error
                    var parent = taggedOrgUnit.SOFDParent;
                    while (parent != null)
                    {
                        var parentTag = parent.Tags.Where(t => t.Tag == OESTag).SingleOrDefault();
                        if (parentTag != null)
                        {
                            taggedOrgUnit.IsValid = false;
                            taggedOrgUnit.ValidationErrors.Add($"Enheden '{taggedOrgUnit.Name}' er opmærket med Adm Org Id '{taggedOrgUnit.OESTagValue}', men der findes en enhed '{parent.Name}' højere i hieararkiet som er opmærket med '{parentTag.CustomValue}'.");
                        }
                        parent = parent.SOFDParent;
                    }
                }
                else // the tag is a child
                {
                    var parentTagValue = Regex.Replace(taggedOrgUnit.OESTagValue, @"-\d+$", "");
                    var parentTagOrgUnit = taggedOrgUnits.Where(o => o.Tags.Any(t => t.CustomValue == parentTagValue)).SingleOrDefault();
                    // Check if the parent exists
                    if (parentTagOrgUnit == null)
                    {
                        taggedOrgUnit.IsValid = false;
                        taggedOrgUnit.ValidationErrors.Add($"Enheden '{taggedOrgUnit.Name}' er opmærket med Adm Org Id '{taggedOrgUnit.OESTagValue}', men der findes ingen enhed opmærket med Adm Org Id '{parentTagValue}' i SOFD.");
                    }
                    else
                    {
                        taggedOrgUnit.TaggedParent = parentTagOrgUnit;
                        taggedOrgUnit.TaggedParent.TaggedChildren.Add(taggedOrgUnit);

                        if (!appSettings.AllowedHierarchyMismatchUuids.Contains(taggedOrgUnit.ParentUuid))
                        {
                            // Check if the parent tag orgunit is also a sofd ancestor                        
                            var parent = taggedOrgUnit.SOFDParent;
                            var parentFound = false;
                            while (parent != null)
                            {
                                if (parent == parentTagOrgUnit)
                                {
                                    parentFound = true;
                                    break;
                                }
                                parent = parent.SOFDParent;
                            }
                            if (!parentFound)
                            {
                                taggedOrgUnit.IsValid = false;
                                taggedOrgUnit.ValidationErrors.Add($"Enheden '{taggedOrgUnit.Name}' er opmærket med Adm Org Id '{taggedOrgUnit.OESTagValue}', men enheden '{parentTagOrgUnit.Name}' med Adm Org Id '{parentTagValue}' ligger ikke hierakisk over denne enhed i SOFD.");
                            }
                        }
                    }
                }
            }
        }

    }
}
