## Usage

curl -v http://localhost:5000/api/person?cpr=xxx&cvr=yyy

# Initial datbase setup

do not create initial schema, the migration will do that

$ dotnet ef database update

# Creating new migration files (examples below)

$ dotnet ef migrations add InitialCreate
$ dotnet ef migrations add AddedLastSync
