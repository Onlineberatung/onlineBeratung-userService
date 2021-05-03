# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

### [2.10.1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.10.0...v2.10.1) (2021-05-03)

## [2.10.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.9.1...v2.10.0) (2021-05-03)


### Features

* adapt agency admin service changes of api ([415b744](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/415b744987b6dfd3420a83f532c8f2aa4f9b2cbe))
* add cors mapping for registration endpoint ([55a8feb](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/55a8febb8aed860221b8722707e4b5091c4bcbdc))
* added conversation api ([eb0f324](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/eb0f3244fe74d91d8e27ff8f554711b102c4b831))
* added registration type to session ([dbb98a0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/dbb98a03c3df1606e2d19313d42e8bb0ea17c99b))
* generate user service admin api markdown files ([8247f16](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8247f168d410544fe60cd8cdd6dd28d5a5868d4a))
* Include generated agency service api ([61e0f8f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/61e0f8fa5313795848e5a47ef4b52f96148b1506))
* load agencies for report rules only once ([1adf8b6](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1adf8b63052f55819509db6b2a58af864f65bab6))
* optimized logging ([c152a5f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c152a5f3d33366cdc621ee6888eb7b0a90c122f1))
* provide endpoint to update consultants data ([a81157f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a81157f50fe1e9ffdc4563e285a8c63a3f71b639))
* provide new consulting type for support group vechta ([cca563e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/cca563ed11e9ef993566a496345c036e7e901bd3))
* push generated admin api markdown to documentation repo ([c17ddad](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c17ddada18b23fea0ba93c88d6138f9ac9acfa05))


### Bug Fixes

* correct delete action order ([1dc3889](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1dc3889dac31ef9a050bf5947f4d7fea08ab9930))
* npe in violation builder ([0d9e20e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0d9e20ee40704afd326b3f4cb8af51a3742ff06a))
* prevent rocket chat user deletion when id is not available ([4f151c0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4f151c04f1dcfc3741ec866e7c3b169d12c8498c))

### [2.9.1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.9.0...v2.9.1) (2021-04-12)

## [2.9.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.8.1...v2.9.0) (2021-04-12)


### Features

* adapt admin consultant agencies to match react admin requirements ([97ccb88](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/97ccb88cb94ecbc79c2d9758ce47cbc5f691dd9f))
* adapt api endpoint to delete asker ([8f4b357](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8f4b357a4aa5809d1cced0a7b6616142548afc39))
* adapt id in response model ([e8b23e8](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e8b23e899d24005683d308cd64a23207544be9df))
* added new call to save session data ([4bb36e4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4bb36e4349cc82c0c0a058c21c5358a01a7a72aa))
* added x-reason header when registering a new user ([219d803](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/219d8037eb9aeae403d47d7f6a36bda64ebb71db))
* changed welcome message text for u25 ([f2e08a5](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f2e08a5354af9962b518729cdf3e5e51fef8a90f))
* integrate deletion of asker ([a708983](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a70898320646f2255a5a13f891a208e112446f1d))
* switched java version for docker build, github actions and maven build to 11 ([6f486b9](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6f486b9fba8a0299c46167026aa3d085abbbb3fb))
* write violation reports to file ([ad22e51](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ad22e510c25f8a64bd776b24dad882d73abeaecc))


### Bug Fixes

* corrected check when sending mail notifications for team sessions ([5fb273c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5fb273c0fb237e9446e8103e6dd832b9a8c0c05e))

### [2.8.1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.8.0...v2.8.1) (2021-03-22)

## [2.8.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.7.1...v2.8.0) (2021-03-22)


### Features

* adapt api changes to match react admin requirements ([ba60865](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ba60865772176aaea46fc657fb98268055312c0d))
* adapt email change on database and rocket chat ([76a293a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/76a293afcc03d9cb69daf8e29d26b9637d408305))
* add documentation and delete action for asker agencies ([2b22613](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2b2261350bfdd5c610841b6408db659e5fa66084))
* add endpoint to set mobile device token ([0b7d1b9](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0b7d1b9e816c985945f0810927a951cb3dac2895))
* added call to mark an asker's account for deletion ([7dcdb85](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7dcdb852be7a00eb8c28e5307c1d346d59b5534c))
* added configurable csrf whitelist header property ([5fab78b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5fab78b21852715ffbe4bc65c0f870c068ff0f65))
* added further steps message to enquiry message process ([3e8d2e6](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3e8d2e63db2f8bc03a9ba5e5e4daaca7c11abae3))
* added message service api client generation ([3da47b8](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3da47b800942be6f8b4a7d3a81c1c649c7bc32cf))
* append user and consultant repository to retrieve all deleted user accounts ([a159768](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a1597687e7ab3a8ee27191f189cd8b9c3ec4aa82))
* clean registration model ([d275a66](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d275a6688ffeca0fd05614272f4fb4ef2f3c525b))
* generate and integrate mail service api client ([1e9cc66](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1e9cc66cd36a8e8af3cf4f8bf12ba15796c50bc7))
* integrate configurable firebase push notification service ([d87424c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d87424c0d1fcb8fe28b8887b3272a743e4ef7f08))
* integrate firebase push message service trigger ([42b21a5](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/42b21a5f91cf5375598ee72ee0f2d24a3eb1a2f0))
* provide deletion framework for user accounts ([da2272f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/da2272fabb1912307bb4df0d669bf03bb22089ec))
* provide persistence for mobile token on user ([5a196dc](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5a196dc8efa09f276e53538a64c5822e3099ea27))
* provide setting and changing for an email address ([6858989](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/685898962f06e37d50b4139f66fae02fdeafc23a))
* spike for u25 push notifications ([f2f43c1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f2f43c13b052931b0caaf1b8e770326abbb45d9a))


### Bug Fixes

* adapt file handling ([07b1867](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/07b18676a0e4ad7d8e8f5147a704262bccde36c9))
* added count parameter to retrieve all group members ([265e095](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/265e09541c9d9dd3e4f933a9639487088854acec))
* change email available check hanlding on keycloak ([e14e47f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e14e47f5d0998071b3d5afeca4c1a5cd73ca5a22))
* do not throw not found when agency has no consultant ([023c28a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/023c28a60080936dc16eb32b879c1a86b0b65917))
* fixed error during email or username already taken check ([e4a2df2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e4a2df2a2e945f3a012565d53606abe89c02946c))
* fixed error during email or username already taken check ([6b2bb8f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6b2bb8f54700cc22fb971365876ee1276599e1ac))
* generalize exception ([f1f9f3f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f1f9f3f646fe2b60323bfdf9065163cb65539ce1))
* remove obsolete test condition ([c6d4cb9](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c6d4cb90d22cccbbe8faaa9b97a1d9618e92042a))
* removed obsolete test ([471df8f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/471df8f2a63f1ce1689522667c46795d122b52ad))

### [2.7.1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.7.0...v2.7.1) (2021-02-23)

## [2.7.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.6.1...v2.7.0) (2021-02-23)


### Features

* adapt logging for monitoring helper and fix potential npe ([9fe2de7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9fe2de7b29c925c63b8517896f224cf4fadc49aa))
* added deleted flag on consultant agency ([823222d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/823222dcea1e3977036253a95429ef9bc129a816))
* change rollback handling to consider all sessions ([a93cdcc](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a93cdccdcfd68001913c871fd0d20b9ba545a10a))
* change wrong credentials keycloak logs to info level ([0646042](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0646042068a4d9baca745fc3b95d41ca5802076f))
* define api to change consultants for agency change ([6674aa8](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6674aa8235bf55390ecf4e902031dee24af932d6))
* extend logging for email notification errors ([a00cd45](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a00cd4518f01e4768b073c79fc38fb43774d5d78))
* implement consultant delete endpoint and validation ([320bb14](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/320bb14644c27700c3a07df2757572b6276f4c82))
* integrate deactivation in keycloak ([e58f66c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e58f66c491e02a985bcbd952a68b61a74c4cd845))
* provide deletion for conaultant agency relations ([3c6d870](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3c6d8708931114ca96755df86d045c56e7292e33))
* provide logic to change agency type from team to default and vice versa ([88e36b1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/88e36b158ecf36b2d23264e244b66f48e657e0cd))
* refactoring of transformation method from dto to entity ([87be2cf](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/87be2cf441ed6189b0c14f6168eedfd9c740b54c))
* restructure api call definition ([afb7db4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/afb7db40056fbf90910d6c1ebcce8f30b4b1d973))


### Bug Fixes

* add where clause to filter deleted agency relations ([0491f59](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0491f59d34a8702a69186dc25c4479baac1c141c))
* consultant id for agency relation ([31715c7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/31715c7bffebddc1e58a3a735e48b22b63759947))
* correct api media type ([9a3957c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9a3957c3b11ce32d407b4d543f6a96d3fc9117be))
* prevent npe while check other consultant agencies ([4d20053](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4d20053df4320c522131ed1e94f777cd7f09317b))
* set values of other keys depending on their structure position ([32e2fca](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/32e2fca5b455a8c7f24d1a60668a3fafa662fea3))

### [2.6.1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.6.0...v2.6.1) (2021-02-08)

## [2.6.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.5.0...v2.6.0) (2021-02-08)


### Features

* adapt new alias model ([8b8aa4e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8b8aa4eb860a4af5199bb2b36d3ea8ac91e58d6b))
* add createConsultantAgency Method stump for further development ([6cae533](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6cae53366ad3775f2c63d0606302388ae65aa018))
* generalize relation creation for reusage in import service ([62af849](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/62af849462a0d68fcf3e281b059775ae05994bee))
* implemented addConsultantToTeamConsulting ([72d6227](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/72d622770f865c241ca3e8f9b6f977d1814784f7))
* initial implementation ([14389b8](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/14389b869aa266db2d6480f499c68705cbf9038c))
* minor optimizations ([5af5c66](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5af5c6682988ede0efcf731f11df50efb20b64bc))
* provide alias object in session list items ([8e1df47](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8e1df47fc2678e6a609d681eb779326ef3c4897c))
* provide asker username via the fetch session api ([11506b4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/11506b415ccd482fa7f21341eb1dddec83ed8946))
* refactored consultingType handling ([21051e8](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/21051e8cabf8df9926550967f016e874e8571b6e))
* refining createConsultantAgency admin call & implementing testing ([01a3ed0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/01a3ed07780abe0845d706a99a7f224181682de8))
* start implementation of create-consultant-agency ([4207579](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/42075795911d442c220e1d4a8b1a24b0723696ce))
* unit and integration tests ([4fd00fe](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4fd00fe30737c9d145438caa99fa6c3196ce8a01))


### Bug Fixes

* added create and update date to consultant agency creation ([d5a022a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d5a022a7cc96552ced46ed85bf824b233cbd7100))
* added missing rocket chat user id value ([a82e486](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a82e486f4f624a3d7a638f80f341bbdad16bb33c))
* fix merging mistake ([c7606a3](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c7606a3f77658adb0261db4a945035ddd9e4777b))
* handling for unchanged email address ([c636182](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c636182dc9a316f73c994e71bfe83f13a09e6182))

## [2.5.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.4.1...v2.5.0) (2021-01-11)


### Features

* add basic consultant agency admin get endpoint ([c716437](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c716437658bd8a13a0a4a23953d7db0d1a7ff869))
* add getConsultantAgency Method stump for further development ([bd7ca8e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/bd7ca8eb54e00772569bb07b745bc1c4f8fe09ca))
* add handling for unknown consultantId ([eec902d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/eec902ddb39d61ab0f8e59bf9a5f11a8754bc668))
* added api limits ([f0d1498](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f0d1498e34783833b33dd1571ad343658522b3b3))
* added ConsultantAgencyAdminResultDTOBuilderTest ([830eb1a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/830eb1a329011a0ac52733ad2e865259b53077d1))
* added ConsultantAgencyAdminServiceIT ([dc45888](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/dc458883fb35a3856d0afaf239a4a5d7e462af10))
* added create consultant agencies admin spec ([f569965](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f569965377b6426f4e6ecbe8530fe939c0f43f03))
* added createDate und editDate to ConsultantAgencyAdminResult ([6c674e1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6c674e1a6bd300ceb643a444b2ee8c72aa9b1595))
* added delete consultant agencies admin spec ([df2f45c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/df2f45c8bc7a8883a426438cfc60687257662f04))
* added get consultant agencies admin spec ([89a6dd7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/89a6dd7b9b7a9d78aafe0a7c1f3a78980b810df1))
* added getConsultantAgency test ([540a678](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/540a6782b6a97f6390129e6027b8c37b7044ca62))
* changed postcode validation for registration ([f5f04ac](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f5f04ac6c1e3b9ae32ba6afdc29a533c3f6dbc9d))
* correct api definition and hal links ([7ee6c41](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7ee6c4178955d87c933b2cd82b04dd1a7eb5f238))
* create needed hal links ([0b050a0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0b050a00fca0369b314c25965a18825b110c9a35))
* integrate api endpoint ([d9d221b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d9d221ba24d6f9804819c6360dbd5a433cf173fe))
* Integrate update endpoint logic ([6ae40ca](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6ae40ca876a3233890c6b8f437bdc891c10d0806))
* minor changes ([f4931d1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f4931d19d3283114e66312dca67d98f788c74811))
* optimize keycloak access, remove unused close call ([c936aee](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c936aeeb97795180f9e1f880d7c01e8f86a21b85))
* provide basic service to create new consultants ([f44a49d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f44a49d80b405578607c191ed88615ad63196f18))
* provide custom error handling for different cases ([2bb55e4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2bb55e429b61b98ad4a06691b05a16ff5a2f7857))
* provide new consulting type for men ([78dcfe7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/78dcfe7245f1390386c15a69e0ca95654c0944c0))
* refactoring ([45bd648](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/45bd64835abc240a0f25633a0f5b5f060dfbae4d))
* update consultant data changes also in rocket chat ([26ea1ad](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/26ea1ad94f340aa7bf5ea9e2be3a4e4b5551130d))


### Bug Fixes

* added MockBean so legacy test are unaffected ([25a9a95](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/25a9a954f134070b3febc7749862cacaf69ec93b))
* changed type of _embedded ([8b765a6](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8b765a6910a10d461129e551160c2600f130ff22))
* fixed merging mistakes ([b2d7c08](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b2d7c087db0663694fff8af9e078832b7a94a53e))
* fixed merging mistakes ([705506d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/705506d92b80c122584611d637786ee19b5620e6))
* session hibernate ([142551c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/142551c281de243d658e8e566cc6d8307ba4c48f))

### [2.4.1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.4.0...v2.4.1) (2020-12-14)


### Bug Fixes

* added extended logging for registration ([e27209a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e27209a315e76e9c94d7453b22f96aae4867b6c7))
* added userid to extended logging ([2e44af1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2e44af1faf7ecbe19f80c8a1acf66d33b309445f))
* codestyle violation ([98119b3](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/98119b3cf3046f7851dd4e02b1c41f27998bb938))
* codestyle violation ([a9a7258](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a9a7258f9bcad3674835f2f403fbb8515fdd9834))
* resolved merge conflicts ([89f1d7c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/89f1d7ce53d7901f4c5d7b2b5d6d9ed4513eae49))

## [2.4.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.3.0...v2.4.0) (2020-12-14)


### Features

* added admin call to receive all consulting types ([0f3ee2a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0f3ee2adec893d44b7866314652786eac2d1718e))
* added create consultant admin api spec ([8bafb71](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8bafb716c64fd9d0fe1fb5d2decd57a2c1bfc329))
* added delete consultant admin api spec ([6f02682](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6f026822c8f8090ff7913a08b9902a3fc0ad4171))
* added get consultant admin spec ([6d14199](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6d14199761a92e0a985a369cb64d566e31b063b0))
* added get consultants admin spec ([8264a37](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8264a37e1b0356e4f5056991cdd469cb43e88462))
* correct rule for wrong team consultants ([ff0d45d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ff0d45d902405e6e4f1299683d5d2285a8b5ccb5))
* define api for admin user sessions ([8b41288](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8b412882ac1fcee0321f07ab229c4a0ef837dfc0))
* implement filterable consultant endpoint ([c82ef06](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c82ef0678c7e0b95c198db1a91faf290e838d649))
* integrate basic violation rule generation framework ([eb17a96](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/eb17a9645465dff94aca677265e2cf7f37779f9d))
* integrate service to retrieve filtered sessions ([9e83880](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9e83880384e64a7a221aa3d02ee8b4ff2d964209))
* provide endpoint for a single consultant resource ([8061734](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8061734b96b32aa71d57074c558576e80834c074))
* provide filtered search for consultants ([3b60a7e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3b60a7ed411cd8f4ae907f1f969161137cc579e5))
* provide hal link generation and controller integration ([831c2e5](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/831c2e503e2a52a11f78709541c2a260d2c19578))
* provide new authorization role for user admin ([1962b75](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1962b75b0e536445a80f15e33c6dbc5287790b14))
* provide pageable repository methods for sessions ([b9bc8bf](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b9bc8bfcb098b95879104fa8763f26753b76da92))
* provide pagination and link generation ([75b0470](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/75b047015d7adafba5fdd610ddde1c2a8eac4e6e))
* provide rule for askers without session and chat ([526961a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/526961af26012144c2a83ce02cdfb122de057797))
* provide rule for consultants in team agency without required flag ([c320052](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c320052906220358287d561517b7fad584db5c05))
* provide rule for consultants with deleted agency relation ([9be3732](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9be37322ee71f38fb411950b5622882b67db93d4))
* provide rule for consultants with wrong team consultant flag ([1f9cf1e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1f9cf1eb35627ca363f2cf45ef09a8beb69e168b))
* provide rule for consultants without agencies ([65aa15a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/65aa15ad1840b326605aef14e6984ce28c0e131c))
* provide rule for missing rocket chat rooms ([9499025](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9499025876adff0f764d8bbb6faa0394d11f259d))
* refactoring of UserDataFacade and unit tests ([77c378e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/77c378e90a29b4582599619d84f5000095e85987))
* temporary implementation of new registration for kreuzbund ([86b477a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/86b477a1e0baf17ee8b6777aee8b18eeef6acd05))


### Bug Fixes

* behaviour of next link ([2d1750f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2d1750fb93093c6728ffc76e323a8ee980d7cba9))
* checkstyle violations ([433c639](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/433c6390a36b050a88e12658310d46cf35b67add))
* emoved unnecessary mock ([30e1552](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/30e1552376f51d3b867246dba735e05e669170a8))
* fixed code style issues ([b48c3cd](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b48c3cd87fdaa7d9854a3664ef37466d3c9a1885))
* provide creation and update date for new sessions ([5adfa89](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5adfa897216201be962f32d1b10de042cfaafe43))
* remove open in view property to prevent lazy initialization error ([28f9eab](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/28f9eab748ec2d5e8571a44eb0b5e6045ad00de3))
* removed unused import ([f0dbb2e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f0dbb2e0063eef184fcb8829c93682702f2b8801))
* removed unused import ([1298a96](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1298a96ad24d5a38cc374064ba5cd31893d95f98))

## [2.3.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.1.6...v2.3.0) (2020-11-23)


### Features

* Improved list handling ([2790205](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2790205b6adda9fd41aa3b4caaa48c81af0db87a))
* Improved logging in SessionListFacade ([c834607](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c8346076875853575d6552623aaa1849ad820cf6))
* prevent live event trigger for empty lists and for enquiries ([c48edac](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c48edac8228b6f128f8d73cc3ed4b69dc104b3cf))
* update keycloak to 11.0.2 ([e7ab339](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e7ab339b6d03cee98405777def5bea053f62239d))


### Bug Fixes

* add resteasy again due to need for keycloak user registration ([a337b96](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a337b960c92a9ce6e3b06b5d20fda620fde3e98b))
* decrement resteasy version to match current keycloak integration ([6fafa80](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6fafa8091899bde83b80fb886bf7da1723db1bf5))
* logging to files ([f94f999](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f94f999f43189dc831abbe2dd44bb2e03a309bcf))
* update deprecated logging property ([14c758c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/14c758c7f74f04c975d52e1fe2a95346e6585c48))

## [2.2.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.1.5...v2.2.0) (2020-11-03)


### Features

* restrict release action to branches starting with release ([44449e2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/44449e2c6e7c19b979cb512662edb7f32e155239))
* update basic spring boot parent version to newest 2.3.5 ([bf1300e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/bf1300e1ee4b6fdeb2edeba60711f1e5177434f6))
* update dependencies ([1e3c4b8](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1e3c4b8e253d9945b87905137863176ac8684d2c))

## [2.2.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.1.5...v2.2.0) (2020-11-03)


### Features

* restrict release action to branches starting with release ([44449e2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/44449e2c6e7c19b979cb512662edb7f32e155239))

### [2.1.5](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.1.4...v2.1.5) (2020-11-02)

### [2.1.4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.1.3...v2.1.4) (2020-11-02)


### Bug Fixes

* concurrency safe keycloak mail check ([aeb6421](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/aeb6421ed0c2d1739d28bdfbf1ae3b5eaf595fea))

### [2.1.3](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.1.2...v2.1.3) (2020-10-29)

### [2.1.2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.1.1...v2.1.2) (2020-10-29)


### Bug Fixes

* correct bean handling of authenticated user ([03b45ea](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/03b45ea70ef1e7c48c9376966c91823165a435f1))

### [2.1.1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.1.0...v2.1.1) (2020-10-28)

## [2.1.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.0.1...v2.1.0) (2020-10-28)


### Features

* adapt logging for consultant import ([c7608ad](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c7608adb9b2815dc19abd3b6d3dfa2e69b96c92a))
* add endpoint and controller for triggering live events ([882c195](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/882c19546e526e18a46c774be3d15f1d118cc5f5))
* added missing removed files ([e02e45d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e02e45dbc50008b1aa9f842f34ecb02e428d44aa))
* correct role configuration ([b3b58e7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b3b58e71fe37da65f1182e8874c75fc527acc9e6))
* correct session finding, adapt security config ([cd4a33f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/cd4a33f5df65784d67fdf8e60e90c8d81c83f743))
* minor optimizations ([6c7317e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6c7317e7d9dcfc7a2b75ac88fde13546f33951d8))
* minor optimizations ([50ad06a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/50ad06a691af43afee6310a5714afc4f202689c6))
* provide logic to collect relevant user ids ([b29ab8f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b29ab8f924d3417ee737f9fd06ec701af778215f))
* setup and generate live service api client ([a716999](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a7169995fa6a067990d0113bb9d07d0d17cec3f6))
* update swagger to openapi ([eed0650](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/eed065019a9f22d33bd9eae1c02fba7c5327c516))
* update to open api v3 and generate models ([94b9c1d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/94b9c1d959f9e7997740ef1ea8cc575328b61444))


### Bug Fixes

* changed swagger package name to match sources ([ac68864](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ac68864d37d3c026afd920ea37fdc972ca01a94d))
* correct security authorizaton ([91e5224](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/91e52241e9afcecd8bb748c9aeea1db65f1beb4a))
* mapped email of user to profiles response ([0f2f54f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0f2f54f95b9a621caa3173c423984d5b413b5ef2))
* removed false character ([4721afd](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4721afd600088398227bb6d38a34233e7f223271))
* set user mail only if its no dummy address ([772d096](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/772d0962cf31118ff0feb11514859e80fc958a37))

### [2.0.1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.0.0...v2.0.1) (2020-10-12)


### Bug Fixes

* added check if rocket chat user id is set ([9674800](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/96748007685559618c6012de88aeb7e8c8bd13ff))
* added package-lock.json to gitignore ([2669f30](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2669f30c18a4e8583d1192cd48650969cca82eab))
* failed tests ([e0da83d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e0da83d032e6e4bd1b0661ad5d2373a98dce2943))
* fixed corrupted tests ([9d19eab](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9d19eab0528d0fb6130fc4508d6f79852e171f00))
* remove package-lock from gitignore ([87eba10](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/87eba106211abf0aed29739f53e6cedab2788a2e))
* wrong parameter javadoc ([f620eb7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f620eb749e4fc7b110be5961a1cdc12329782072))

## 2.0.0 (2020-07-29)


### ⚠ BREAKING CHANGES

* changed URL of call to register an asker from /users/askers to /users/askers/new
* session id path parameter is now mandatory
* changed URL of call to write an enquiry message from /users/sessions/askers/new to /users/sessions/{sessionId}/enquiry/new
* session id path parameter is now mandatory
* changed URL of REST API call to write an enquiry message from /users/sessions/askers/new to /users/sessions/{sessionId}/enquiry/new
* session id path parameter is now mandatory

### Features

* add session data for new consulting type registrations ([a1deed0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a1deed0d8e19d48fc67123be32bc595692994027))
* added agency and city to GetUserDataFacade ([ddde8cf](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ddde8cfba9cf7fd1e66a40c69d84027e4391337b))
* added new API endpoint to register new consulting type sessions ([c33e6aa](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c33e6aaba2b27e86ed53ae2710c65c0655a525b8))
* added new call to register new consulting type sessions ([fcafd12](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fcafd122132ccef94df779a2aa9f82ce0d90fd13))
* added session id path parameter to create enquiry message call ([4207a36](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4207a36a20cec3a0c0eeec22866d56efa1953fa8))
* added session id path parameter to create enquiry message call ([e3cf77e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e3cf77ed6e8aacb70ba3c5499605446f6268193a))
* changed AgencyServiceHelper to use new agencies endpoint ([7536605](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7536605611421888278b55ec248eb1140b96b7a7))
* changed structure and included agencies in GetUserDataFacade.getUserData, AgencyServiceHelper now uses new endpoint ([3656e61](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3656e612f571524eb7efee17cd97fc588573307c))
* changed the sessionData in the response to consultingType which includes sessionData and isRegistred ([45dc66a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/45dc66a1f5f8c03a8d4387d9766104f1276974c5))
* extend the asker session list for new registrations ([e3353cf](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e3353cf27729c4e259bf9b2b0d8498b09e1ff553))
* Initial Commit ([77f8644](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/77f86442a8e631ccc43f4b0898458f9423b17879))


### Bug Fixes

* added check if Rocket.Chat user ID is set when getting session list ([5e88cbc](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5e88cbc16e084860e8718235947c16dd7eb3b6ad))
* added check if session belongs to calling user ([9f3d370](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9f3d370eca886d4665c6cd4e461ee77eae01e1b7))
* added npm install for github release action ([8470469](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/84704697f28a743b7e0b8043bec162bb539b3325))
* fixed incorrect isRegistered flag for chat consulting types ([ba2c3b4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ba2c3b46a06345e74ab7e6a2c77ba3f781760b89))
* fixed incorrect javadoc ([46fb329](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/46fb3298ed77bed2eaea60a464af96218a571a6f))
* fixed unit test ([9c78827](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9c78827fa77233f0ab878adf2dbcb6cb8ee5c01d))
* implemented change requests ([fe86a39](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fe86a39a7acc0de1d6a3f15be7fd65843a411bf7))
* removed bug when writing enquiry messages for feedback groups ([013840e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/013840e363182a5910391c3e53cffbceeeb8bd5c))
* removed empty check ([2ede6fe](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2ede6feb9c15db2d73a174abf1cf11f9f1eb7db8))
* removed unneeded init ([d4dc2c4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d4dc2c473210c9d751ab64e0ee827289f3602d90))
* return empty list instead of exception when no sessions available ([1956061](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/195606195717e2167f525755ea8ea05e5111aa7a))
