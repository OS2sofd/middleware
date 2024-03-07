ALTER TABLE municipality ADD COLUMN function_map VARCHAR(255) NULL;
UPDATE municipality SET function_map = concat(concat(sr,'=SR'),';',concat(tr,'=TR'),';',concat(tr_suppleant,'=TR_SUPPLEANT'),';',concat(medudvalg,'=MED_UDVALG'),';',concat(amr,'=AMR'));
ALTER TABLE municipality DROP COLUMN sr;
ALTER TABLE municipality DROP COLUMN tr;
ALTER TABLE municipality DROP COLUMN tr_suppleant;
ALTER TABLE municipality DROP COLUMN medudvalg;
ALTER TABLE municipality DROP COLUMN amr;
