import json
import requests

server = 'https://<DOMAIN_NAME>'
migrationUserName = '<KEYCLOAK_MIGRATION_USER_NAME>'
migrationUserPassword = '<KEYCLOAK_MIGRATION_USER_PASSWORD>'

roleNamesToChange = [
  {
    'sourceName': 'u25-main-consultant',
    'targetName': 'main-consultant'
  },
  {
    'sourceName': 'u25-consultant',
    'targetName': 'peer-consultant'
  }
]

getTokenHeader = {'Content-Type': 'application/x-www-form-urlencoded'}
getTokenBody = {
  'client_id':'app',
  'grant_type':'password',
  'username':migrationUserName,
  'password': migrationUserPassword
}

tokenUrl = server + '/auth/realms/online-beratung/protocol/openid-connect/token'
tokenResponse = requests.post(tokenUrl, data=getTokenBody, headers=getTokenHeader)
print('Fetch Bearer access token from keycloak')
print(tokenResponse.status_code)
accessToken = tokenResponse.json().get('access_token')
authenticationHeaders = {'Authorization': 'Bearer ' + accessToken,
                         'Content-Type': 'application/json'}

def changeRoleName(fromRoleName, targetRoleName):
  roleBaseUrl = server + '/auth/admin/realms/online-beratung/roles/'
  identifiedRoleUrl = roleBaseUrl + fromRoleName
  getRoleResponse = requests.get(identifiedRoleUrl, headers=authenticationHeaders)

  if getRoleResponse.status_code != 200:
    print('Role with name "' + fromRoleName + '" does not exist in keycloak')
  else:
    roleToChange = getRoleResponse.json()
    roleToChange['name'] = targetRoleName

    result = requests.put(identifiedRoleUrl, data=json.dumps(roleToChange), headers=authenticationHeaders)
    print(result.status_code)
    print('Role with name "' + fromRoleName + '" has now the new name "' + targetRoleName + '"')

for roleMigration in roleNamesToChange:
  changeRoleName(roleMigration.get('sourceName'), roleMigration.get('targetName'))
