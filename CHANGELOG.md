# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

### [2.18.1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.18.0...v2.18.1) (2022-05-12)

## [2.18.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.15.0...v2.18.0) (2022-04-26)


### Features

* adapt tests ([c83c1f4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c83c1f41acd1777d753cc0b74d715d0f10829891))
* add 2fa type information to user data endpoint ([f0f2398](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f0f2398a14f3deae1ff813509d7e2f1f099e9d57))
* add agency name to daily notification email, introduce new email template ([31c9c52](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/31c9c5283cc6774fa5532913ba8f5247647e30ce))
* add correlation id ([9622455](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9622455d8a5c686066a55dd9f732c1b1c6e2c0a1))
* add displayname to consultants in session info ([d3cccc7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d3cccc754646a8ccdaacbbf405ea2043c736132c))
* add endpoint to ban users from group chats ([da346c1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/da346c100d710b8d0d07fc8ec2609632dde088c6))
* add endpoint to delete an email ([3a78c80](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3a78c80fcccb49d6b0781ea3710b9a6b28faa323))
* add endpoint to index-search consultants ([0811036](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/08110364f65b8da420bdd71780d9b412a749b71f))
* add language code to session ([d590a62](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d590a62e29a45dc8aeaec6deadc97795a83586f2))
* add language codes to enquiry message ([b2b9714](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b2b9714ce18e68081ee13e7cfa43d39c0193c664))
* add mute call towards Rocket.Chat API ([194fc09](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/194fc096bb03b361b4c54cb33b80ca29c9c7d4bf))
* add response if setup process has been done ([f17016f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f17016fe05259c60f6b68bb6f912a62c1aae8585))
* added status in deletion for consultants ([207ea52](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/207ea5216cc7adcc76d6d9c8c886b81baffb19c1))
* allow new session creation for new consultingtypes ([51d3b0f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/51d3b0f65ba8245e592adeafa9f7b5385134ccfa))
* connect enquiry message language and session in database ([1d65703](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1d657033651facaa9883090b3e9dc5923ecf6073))
* define API definition ([43a4769](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/43a476990dfb5df7748dd3fd43e4e467ce9e8b3b))
* disable user login if user is about to be deleted ([789cdb7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/789cdb7c37751e97e468a346e6721a1be2554145))
* display consultant languages in user data endpoint ([7a89fcf](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7a89fcf3ec82269fe7ec5a65318ffb13a6b2fe52))
* endpoint for deleting an appointment ([3445d1c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3445d1c5d1b7be313cc5eacf192e8cbef6418dbe))
* endpoint for getting all appointments of today ([f35b643](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f35b6431671b06a243e0d5ebe870bb43825170b9))
* enrich consultant reponse with agencies ([543e0c0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/543e0c00240246f716cf5475ee518b8c6dd865cd))
* expose all languages of the consultants who work for a given agency ([26390c2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/26390c2070b38794ff5c29d49f8aeb40845c925e))
* expose set session language in registered enquiries endpoint ([3bd8d54](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3bd8d54a1d07040f182c2146fa4a0370dcf2f441))
* expose single consultant including agencies ([626ea36](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/626ea363ca434533fa90a15a1a472e9dd22bebb0))
* extend user information with display name ([c95e4ad](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c95e4ad419ff3c1c254058c229f69ee0fe0a85b0))
* format log lines ([5ed779d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5ed779d942f3cc259d580d860dcc9b29e3a9444e))
* identify keycloak config port ([f268075](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f268075b0cd30ffa0802a2613920f0c1cf1a78b6))
* integrate mail sending for open enquiries based on agency ([f1fe85c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f1fe85c70a893856602cbadf9a384f91bfdbfca7))
* integrate sort param for consultant list ([1a10f11](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1a10f11bc5cc5545142fa99b937f7e788c699fe5))
* integrated validation for already registered direct conversation ([42c5b37](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/42c5b375b030d62ccb2a6bbab200e7be69da853c))
* introduce end-to-end encryption feature switch ([7c68469](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7c68469e5567632e4cdeff30da0279b8741e1acd))
* job for deleting obsolete appointments ([017b21b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/017b21b1ac6fb2d9e73f14207d821fe5670b95d3))
* let the user speak with encourage-2fa flag ([2b15867](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2b15867bb48c2c271d8b15269ac0c40dbe9e2995))
* provide cronjob for regular notification mails of open enquiries ([8bdac70](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8bdac7082feef5324cec6235c15930ec239aed67))
* provide public agencies for consultant ([b54c7f9](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b54c7f99d29c42961c865ee62d3b255fa4de09c6))
* provide technical solution for direct registration ([1f1ce8c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1f1ce8c32800834e929d292d1111bf2e056f8666))
* save consultant languages ([0e0a991](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0e0a991edc9c9f70d4b55d47f7e3f860f4bc382d))
* save email from 2fa workflow ([2e46a26](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2e46a2652fe4133c19e839bb013e6b5c9e5fec89))
* turn on JSON logging ([89a9140](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/89a9140fc055db4fc0dec2573f8c75f7156967b0))
* update appointment ([830d647](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/830d6471d653970df596c761f7d33afd54194fdc))
* validate incoming language setting ([7eaf9a2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7eaf9a2f0d2f09a1002930a96fa34fcbbaf3f8c6))


### Bug Fixes

* add consistent response to Keycloak API ([fdac9c9](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fdac9c921a6c78eb47251ed47f05ceacd9c6a219))
* add display name to ForwardMessageDTO ([9d3827a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9d3827ac43a7f33967953378151865f887521704))
* add user create date and modified date automatically ([83f20d4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/83f20d4123a4eaf4400b2f5684fd0df4641b179d))
* allow umlauts in email address ([b6f93c4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b6f93c42c8bc218f07595e450543da00f33c2cec))
* api generation of 2fa ([a3a14eb](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a3a14ebd1831142c56d34f3ae52e9e55c52630d0))
* audit user agencies ([ad1464e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ad1464e555ef619ba9387b2a4ef89a23f75f9ae6))
* chat info's auth ([cda81e6](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/cda81e6ac6fd5b96774b3b74d1a11235d1e5eed1))
* check bad request logging correctly ([59a9ece](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/59a9ece2a0067ca5e5d47e95e338073686c85dd2))
* check credentials internally ignoring 2FA ([79b242a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/79b242a90ebfc40f28562d7c5fa2b6b413a57870))
* compare enum names ([d3ecf70](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d3ecf70926e25456a891f43f11fcfaa394abeff9))
* consultant setup in tests ([8181f21](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8181f21f098d938a9a5686193e825bf30552598b))
* create anonymous user bean ([83c7907](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/83c79079f44e1585ed94b6a7bc240f104e1b7637))
* create anonymous user bean ([4b4c99b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4b4c99b99ceb69fb2a1b0342d1f2f316e4b850ac))
* date Spring version ([053d715](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/053d715d2d91a110f37c5cee03f8da4fae5d6f3c))
* delete account ignoring 2FA ([f5505bc](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f5505bc5720a12913b79fd57dbcbb19a99ee7bf8))
* do not lowercase names from Rocket.Chat response ([4147205](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/41472055ec6e32e6c109c743f35229d16b7709c9))
* encapsulate Rocket.Chat user into generic data field ([30d8136](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/30d8136b2c92a2f9c61dc2a5eee0e9f025abbea9))
* encode display name for database entry ([15b01b4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/15b01b4fb057f166a485df6e58192afbff37ce29))
* exclude deleted agencies for not deleted consultants ([1db481b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1db481b6a269f0ae6df438ec79cc67f78bc0b424))
* failing appointments test ([e64b293](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e64b2933b1b3b10bc1fa63a455cb6d11478b84c0))
* fetch additional messages for session if last message is system alias message ([4777eea](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4777eea955124da46bc390e25d70024834b3f8d0))
* fix checkstyle warnings ([c209f33](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c209f3345fb3cf6d19f5e8087fad13bec38f979e))
* fix Swagger glitch ([221116d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/221116dd1a9d6cca96d76717a039f7a83a5ab918))
* force safe log4j version ([1c6bf3a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1c6bf3ac4074dcf219b24dc25e7b48bd24aa868f))
* format and fix signature ([4c47b19](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4c47b1918d5c82a72905bf35baf1456e17b6f783))
* format of ban payload ([3983b13](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3983b13defae467618540a2ddd11b18b3fe1123f))
* Hibernate property access in session data ([76661a7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/76661a766cb7a65b6ca8d060d20ff57fdd74b92b))
* hide consultant names in public endpoint ([dbc7d68](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/dbc7d688d1b45f52479bdf499cd78fcbb8743865))
* impl. review requested changes, formatting ([e03bc3e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e03bc3e78910accf48320606f744255ecf6c2393))
* index out of bounds for no tenant clients ([5b41f6d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5b41f6d0db1ae30837733585f52b37a1c9b5011c))
* logical error in attempts-left flag ([5973011](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/59730111e3f67ce600329b65be6529e9fe8ac5c2))
* mail subject in daily enquiries ([74902c2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/74902c22f9d7ae2dabfd8fe77169b5cbde537b6d))
* make consultant search use index ([a8c210d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a8c210d274770350eaa604f1bda3ba2133729310))
* management of OTP allowance ([481ec22](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/481ec22ac7ea1f95efeb215478c8c659df0819bd))
* mark consultantagency relation as deleted when consultant is being deleted ([ebf8e9e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ebf8e9eb11fbf56144d612b1552a33126185f30a))
* message conversion in REST template ([033cfaa](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/033cfaaff5a3db8340821386dd82d4befe0afa27))
* migration references ([b9cef43](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b9cef43d7d1f5cdfe805d340fe66ef62c0c0cc9c))
* move filter test to correct location ([a80482b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a80482bb53d48c20c8aa1a7a411c5aeb33d53e12))
* multiple Hibernate properties ([d467fc0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d467fc0fe2856469bc23374782d9535a6ea316b4))
* path in auth test ([200f429](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/200f429436a2d6ff1dc0e92e3480de4349405f80))
* permit all users accessing appointment info ([9be0e48](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9be0e48fed45aae67b4775299612e3c4debb61ef))
* permit all users accessing appointment info ([185ab1d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/185ab1d21cb294e14c9bf5e8738588e097915e47))
* potential null pointer exception in keycloak message ([391415f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/391415f43ce34aaa6287b38d91b8fe9c8646f0a9))
* prevent case error in user search ([4188ef0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4188ef0c431ee15ee5509ec84e1f54d2d85258cc))
* prevent finishing 2fa process if email has not changed ([aa9f543](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/aa9f5434ed7229d689bc0bdd094fc832225b21a5))
* prevent starting 2fa process if email is owned by other user ([fe550d2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fe550d28a39208ebb68bd0609f001f9d7859a75c))
* properly clear the mdc ([de52ff6](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/de52ff63c608268b7e5feeabf64db24440bb16fa))
* provide alternative message for last message ([cc83bd2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/cc83bd2f9ca6cb0ac250e37b3dea2b0a6a98738f))
* provide only not marked for deletion consultants by agency ([74fc728](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/74fc7285f157e7f9d50ef0344acbf938d76712d1))
* reference by the owning entity instance in languages ([628febe](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/628febe8c4a5ba57b1a8c7a95f2813547f626769))
* remove faultily generating rc-token requirement ([c37c5ab](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c37c5ab0c89f6ba371a8c55c971a8f77c32703e1))
* remove Hibernate type from monitoringType field ([cd0676e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/cd0676e67d6f873f5ca5713d5865008237144752))
* remove redundant tests ([0af86fd](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0af86fd1f8da8af70d57e690a3fedd3d96c22357))
* remove unused logging methods ([00d460d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/00d460d73071192030c262912132ebdc33600551))
* replace logging methods ([df22905](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/df22905d858b7572ff268ac80f28ffce1dd5bdb3))
* replace logging methods ([53e6f5e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/53e6f5ef0a255542f5b481acda270ca3d97261ac))
* replace stacktrace linefeed characters ([eab0368](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/eab0368f71af320f155e5370bde77321349ac9a9))
* replace user's account id by their messenger id ([5453954](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5453954414a9d0c7da4f7b777b943b9783bb02ff))
* set last name correctly ([ad68590](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ad68590d61ed581a36fcd80fff09f585dbc9173a))
* static sql inserts ([929d637](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/929d637d7ab9b0f2ff1a6f844d66b4f15b71d79c))
* take care of comparing emails lowercase for Keycloak ([8065f63](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8065f63813932a0a9e17e2c2647f59ab49e11d7b))
* tests for mail subject in daily enquiries ([c78bff9](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c78bff9f9fd80568574d88072409f66f9880c90e))
* tests including rcToken ([d884ee2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d884ee29a1a59d225d886f1f35d8b90b38d65cf6))
* typo in stacktrace logging ([d8be04d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d8be04d8557bb86d5d5df633a662cfac53e3f98a))
* unmute by usernames in Rocket.Chat ([26c7083](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/26c7083ff6ea5c1e807687d65279cfd77ee8e0e4))
* update log4j ([a2ab2d3](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a2ab2d3f9e526d4c4f814f1c2bf0ea4ba39d0f57))
* update log4j ([b26ba86](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b26ba86fda5319ec26d0905e47a3242435d5b7f9))
* update message dtos ([4b391f6](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4b391f6870a7e5ce86918b9ca805a9211f6d828a))
* update to saver log4j ([a402246](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a40224617d07f71db35a2cacda5d2b31d7e59261))
* use autowired instead mock in int. test for videochat config ([cbe6acc](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/cbe6acc18ebfc9d9efbb6bb6515e1ccc89a4f71a))
* use configuration-inherent resolution for e2ee property ([7822b08](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7822b08b7a3788034454cef6577e018b024c1107))
* use right displayname key ([c968839](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c968839cded63883874078098e4e98934d143337))
* use right rc-token name ([f99fc8b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f99fc8b2a433e13fbe7005f846b2389e05915f27))
* use system user for banned users info ([4f3c828](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4f3c828077c469f0faff5b795d081cf9f2dae8fc))
* use system user for Rocket.Chat info call ([40f681c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/40f681c5847b49e53b93d4286a4b61b8163f3d21))
* use system user to mute or unmute ([3b37b6b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3b37b6bfa94634715b9e764ff2035094ed66eb2f))
* VariableDeclarationUsageDistance ([4b820a1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4b820a12426d85fb26e3003a2b0474a6e8dc120d))

## [2.16.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.15.1...v2.16.0) (2022-03-14)


### Features

* add 2fa type information to user data endpoint ([f0f2398](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f0f2398a14f3deae1ff813509d7e2f1f099e9d57))
* add language code to session ([d590a62](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d590a62e29a45dc8aeaec6deadc97795a83586f2))
* add language codes to enquiry message ([b2b9714](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b2b9714ce18e68081ee13e7cfa43d39c0193c664))
* add response if setup process has been done ([f17016f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f17016fe05259c60f6b68bb6f912a62c1aae8585))
* allow new session creation for new consultingtypes ([51d3b0f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/51d3b0f65ba8245e592adeafa9f7b5385134ccfa))
* connect enquiry message language and session in database ([1d65703](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1d657033651facaa9883090b3e9dc5923ecf6073))
* expose all languages of the consultants who work for a given agency ([26390c2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/26390c2070b38794ff5c29d49f8aeb40845c925e))
* expose set session language in registered enquiries endpoint ([3bd8d54](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3bd8d54a1d07040f182c2146fa4a0370dcf2f441))
* identify keycloak config port ([f268075](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f268075b0cd30ffa0802a2613920f0c1cf1a78b6))
* integrated validation for already registered direct conversation ([42c5b37](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/42c5b375b030d62ccb2a6bbab200e7be69da853c))
* let the user speak with encourage-2fa flag ([2b15867](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2b15867bb48c2c271d8b15269ac0c40dbe9e2995))
* provide technical solution for direct registration ([1f1ce8c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1f1ce8c32800834e929d292d1111bf2e056f8666))
* save email from 2fa workflow ([2e46a26](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2e46a2652fe4133c19e839bb013e6b5c9e5fec89))


### Bug Fixes

* add consistent response to Keycloak API ([fdac9c9](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fdac9c921a6c78eb47251ed47f05ceacd9c6a219))
* add user create date and modified date automatically ([83f20d4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/83f20d4123a4eaf4400b2f5684fd0df4641b179d))
* api generation of 2fa ([a3a14eb](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a3a14ebd1831142c56d34f3ae52e9e55c52630d0))
* audit user agencies ([ad1464e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ad1464e555ef619ba9387b2a4ef89a23f75f9ae6))
* compare enum names ([d3ecf70](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d3ecf70926e25456a891f43f11fcfaa394abeff9))
* fetch additional messages for session if last message is system alias message ([4777eea](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4777eea955124da46bc390e25d70024834b3f8d0))
* fix checkstyle warnings ([c209f33](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c209f3345fb3cf6d19f5e8087fad13bec38f979e))
* Hibernate property access in session data ([76661a7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/76661a766cb7a65b6ca8d060d20ff57fdd74b92b))
* impl. review requested changes, formatting ([e03bc3e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e03bc3e78910accf48320606f744255ecf6c2393))
* logical error in attempts-left flag ([5973011](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/59730111e3f67ce600329b65be6529e9fe8ac5c2))
* multiple Hibernate properties ([d467fc0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d467fc0fe2856469bc23374782d9535a6ea316b4))
* path in auth test ([200f429](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/200f429436a2d6ff1dc0e92e3480de4349405f80))
* potential null pointer exception in keycloak message ([391415f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/391415f43ce34aaa6287b38d91b8fe9c8646f0a9))
* prevent finishing 2fa process if email has not changed ([aa9f543](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/aa9f5434ed7229d689bc0bdd094fc832225b21a5))
* prevent starting 2fa process if email is owned by other user ([fe550d2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fe550d28a39208ebb68bd0609f001f9d7859a75c))
* provide alternative message for last message ([cc83bd2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/cc83bd2f9ca6cb0ac250e37b3dea2b0a6a98738f))
* remove Hibernate type from monitoringType field ([cd0676e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/cd0676e67d6f873f5ca5713d5865008237144752))
* take care of comparing emails lowercase for Keycloak ([8065f63](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8065f63813932a0a9e17e2c2647f59ab49e11d7b))

## [2.15.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.13.3...v2.15.0) (2021-12-14)


### Features

* added test classes for RequestHelper and KeycloakTwoFactorAuthService ([34ef811](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/34ef811d5b17f9272d13a993e01b5e2c057a4384))
* disable user login if user is about to be deleted ([789cdb7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/789cdb7c37751e97e468a346e6721a1be2554145))
* finished Keycloak2faService extension ([893ba94](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/893ba949948b991bee01c6e96649dc169036837c))
* implementation of StatisticsService, trigger ([c072c6d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c072c6d05360b5bbe7e18ae9531781be9b040451))
* implementation of unit and integration tests, optimizations, added statistics event to AssignSessionFacade ([23b9b20](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/23b9b20868ac1f7ce360125063cf930acff9c8d8))
* merged caritas dev ([1933f2c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1933f2cd9ab3cf1d6f0717c747a5e8bb25c6a97a))
* new message format ([e7cee5d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e7cee5d43fe9b76110bbd192dcbe65993ce1f1bc))
* new statisticsservice.yaml with changes in create message statistics event ([2328ed8](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2328ed8e70260436176e33ee93d982a0dc70c028))
* optimizations ([4837b07](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4837b07daf39e08c8673cf75769d36826989042b))
* optimizations ([e19d8e1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e19d8e1568397696bc9122f3599503fad1bda8d8))
* outsourced header generation ([297d1d7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/297d1d767feb997d84a5e642d24dd5e275fcb66c))
* removed statistics authority ([d6deaa5](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d6deaa50b55a64c0c28c7684569433857da84869))
* started adding tests for new endpoints ([f617c1f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f617c1f82689333ea5b6c095b94c199e75e75058))
* update of statisticsservice.yaml ([e276d67](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e276d67678ef28fac2302a40814cab6edd53c2bf))


### Bug Fixes

* adjusted test ([fb47265](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fb472654cca2b25a289cb8e1996318c85a01976d))
* adjusted tests ([03b3fb1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/03b3fb12881e52697d1668d315b1962de81a6a39))
* adjusted yaml file ([df1c253](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/df1c253d8301f6a8286e8a80abe2d9724d656347))
* changed 2fa to twoFactorAuth ([81280e7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/81280e7177f5120095b9be4a3024466e9ee93cf8))
* changed 2fa to twoFactorAuth ([facfbcd](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/facfbcdd6af848c6122d7bdd2eb08e5aa4c8cbc6))
* changed error handling, added test cases ([a162e0d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a162e0d49f4f8b6087a6974801ce0fdde56e4cf9))
* changed error print ([66c0c7e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/66c0c7ec194b3406beb4817d116aba1dec158106))
* deleted empty file ([de28e4e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/de28e4eca63c5692ca4e5f1336d0861bb9fa6631))
* fixed parameterized tests ([5aa52f2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5aa52f265e9d48107bfa6ffd2a8f722f56d1305a))
* fixed sonar issues ([fb99a9a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fb99a9a45f66e544e9e1ea1c4330768a1c833cf8))
* fixed typo ([2946311](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/29463111de2d7a03fc261d6f8dbae1fa9dd94d6d))
* formatted, fixed sonar issue ([395c470](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/395c4705f1da8d8534b40c1cdc71bd5f8ab54c8d))
* moved logic away from controller, error handling now with exceptions ([4ade2f5](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4ade2f5bb7343c9c98bcea55256fbab919695647))
* outsourced code ([a3f94df](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a3f94dfe7d338a975748b90c448b7ba3f9cb6e39))
* refactored duplicated code ([82d7433](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/82d74334a147ee922bf753f0f49f145fede21fdf))
* removed exception from private constructor ([c4500d1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c4500d1971a227c11cb50f9e6bd3bd352747d0bb))
* removed outsourced tests ([da0fd4d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/da0fd4d79da224aebce88e4934648a4f421e495b))
* removed servers from yaml file ([56ef654](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/56ef654189ea79ccc948ac127c4d0678d24ee09d))
* removed unnecessary endpoint ([1584f49](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1584f49c3e184092c0420bc74b4daaaedaecd928))
* removed unused methode ([fd9aa36](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fd9aa3685bb329cd06dd074bbc9fd9f00a900c6d))

## [2.15.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.13.3...v2.15.0) (2022-03-18)


### Features

* added test classes for RequestHelper and KeycloakTwoFactorAuthService ([34ef811](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/34ef811d5b17f9272d13a993e01b5e2c057a4384))
* finished Keycloak2faService extension ([893ba94](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/893ba949948b991bee01c6e96649dc169036837c))
* implementation of StatisticsService, trigger ([c072c6d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c072c6d05360b5bbe7e18ae9531781be9b040451))
* implementation of unit and integration tests, optimizations, added statistics event to AssignSessionFacade ([23b9b20](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/23b9b20868ac1f7ce360125063cf930acff9c8d8))
* merged caritas dev ([1933f2c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1933f2cd9ab3cf1d6f0717c747a5e8bb25c6a97a))
* new message format ([e7cee5d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e7cee5d43fe9b76110bbd192dcbe65993ce1f1bc))
* new statisticsservice.yaml with changes in create message statistics event ([2328ed8](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2328ed8e70260436176e33ee93d982a0dc70c028))
* optimizations ([4837b07](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4837b07daf39e08c8673cf75769d36826989042b))
* optimizations ([e19d8e1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e19d8e1568397696bc9122f3599503fad1bda8d8))
* outsourced header generation ([297d1d7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/297d1d767feb997d84a5e642d24dd5e275fcb66c))
* removed statistics authority ([d6deaa5](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d6deaa50b55a64c0c28c7684569433857da84869))
* started adding tests for new endpoints ([f617c1f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f617c1f82689333ea5b6c095b94c199e75e75058))
* update of statisticsservice.yaml ([e276d67](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e276d67678ef28fac2302a40814cab6edd53c2bf))


### Bug Fixes

* add technical tenant context for crons ([1d12c65](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1d12c655bd4452c771b713acfec1e43a85e14834))
* adjusted test ([fb47265](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fb472654cca2b25a289cb8e1996318c85a01976d))
* adjusted tests ([03b3fb1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/03b3fb12881e52697d1668d315b1962de81a6a39))
* adjusted yaml file ([df1c253](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/df1c253d8301f6a8286e8a80abe2d9724d656347))
* changed 2fa to twoFactorAuth ([81280e7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/81280e7177f5120095b9be4a3024466e9ee93cf8))
* changed 2fa to twoFactorAuth ([facfbcd](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/facfbcdd6af848c6122d7bdd2eb08e5aa4c8cbc6))
* changed error handling, added test cases ([a162e0d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a162e0d49f4f8b6087a6974801ce0fdde56e4cf9))
* changed error print ([66c0c7e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/66c0c7ec194b3406beb4817d116aba1dec158106))
* deleted empty file ([de28e4e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/de28e4eca63c5692ca4e5f1336d0861bb9fa6631))
* fixed parameterized tests ([5aa52f2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5aa52f265e9d48107bfa6ffd2a8f722f56d1305a))
* fixed sonar issues ([fb99a9a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fb99a9a45f66e544e9e1ea1c4330768a1c833cf8))
* fixed typo ([2946311](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/29463111de2d7a03fc261d6f8dbae1fa9dd94d6d))
* formatted, fixed sonar issue ([395c470](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/395c4705f1da8d8534b40c1cdc71bd5f8ab54c8d))
* moved logic away from controller, error handling now with exceptions ([4ade2f5](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4ade2f5bb7343c9c98bcea55256fbab919695647))
* outsourced code ([a3f94df](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a3f94dfe7d338a975748b90c448b7ba3f9cb6e39))
* refactored duplicated code ([82d7433](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/82d74334a147ee922bf753f0f49f145fede21fdf))
* removed exception from private constructor ([c4500d1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c4500d1971a227c11cb50f9e6bd3bd352747d0bb))
* removed outsourced tests ([da0fd4d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/da0fd4d79da224aebce88e4934648a4f421e495b))
* removed servers from yaml file ([56ef654](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/56ef654189ea79ccc948ac127c4d0678d24ee09d))
* removed unnecessary endpoint ([1584f49](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1584f49c3e184092c0420bc74b4daaaedaecd928))
* removed unused methode ([fd9aa36](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fd9aa3685bb329cd06dd074bbc9fd9f00a900c6d))

### [2.13.3](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.13.2...v2.13.3) (2021-12-09)


### Bug Fixes

* remove async remocval of rocket chat members ([2495897](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/249589788e748e6b0cc1854efaadb257aea4c827))

### [2.13.2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.13.1...v2.13.2) (2021-12-03)


### Bug Fixes

* mock only lowercase main consultants in test ([a09a963](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a09a963b5db44db8a986a8cf9ec9056a6c5eb42b))

### [2.13.1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.13.0...v2.13.1) (2021-12-03)


### Bug Fixes

* case issue on user role value ([d8c978e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d8c978ea35de66839f9994613f02aa34469ac46b))
* email only consultants who are assigned to the session or main ([bad1bba](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/bad1bbaff491de6d988e28b7d5a401a3f3ca6631))

## [2.13.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.12.1...v2.13.0) (2021-11-29)


### Features

* add auth for consultants only ([c1f5f7d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c1f5f7dbbb61fbdce1fca9dc2102974851c095f4))
* added authority for main consultant to assign consultant to peer session ([3063f6c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3063f6c04ff1a414aa06d94ddd0895ab4c1ffea6))
* added buildx setup ([1eac21c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1eac21cd654f63f25552b4d4458f3c89c10dd3ad))
* adjust role authority mapping ([e28fa99](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e28fa99aa3c6587fc90aadcf7fc9a53eef5fc2ab))
* check user search by tomorrow date ([3800fe2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3800fe2af429841a034c50eb6506c400d0c2935c))
* define endpoint for session deletion ([46981d4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/46981d4b84228fcb9a5c0328ec8608eff57ba007))
* delete users without sessions ([14dab47](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/14dab47cd5fa6481844c7e37ddc492378e26b7f4))
* detail client error by not found ([d37c7a3](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d37c7a301dc5ef39fec00d833a25fd50dd925d0c))
* extend push notification service to provide multiple mobile tokens ([85e0da7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/85e0da732ab30456b70dc49fe87f9dd9ace29423))
* implement delete-session endpoint ([ca564c6](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ca564c6695346a691ee611174f6ee58291e9351e))
* provide attribute is peer chat for all sessions ([92a0b3d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/92a0b3d53492613446accfa4793e3afca6b65440))
* provide endpoint to add mobile tokens for user accounts ([2982ab1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2982ab18827a7b96a07ee72a5340ffe868a7a2ba))
* provide keycloak name migration for u25 main consultant and u25 consultant ([9204e72](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9204e72bde00b012151a9225d67f3af96a207578))
* provide mac m1 architecture ([82c0031](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/82c0031a980b1a196d74e85e8153260035c9427f))
* provide multiple platforms ([7b7390a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7b7390a42d6fdef2176ea9189eb547d02d0b4d81))
* remove rocket chat group members asynchron ([51e0fc9](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/51e0fc909f0be3267a03f39ae244524b94e311bb))
* remove rollback remove users function ([e574e26](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e574e266a41c4953f49aa382f2538ffef3a26c4b))
* respond with session id and rc-group id upon enquiry message creation ([ba5b892](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ba5b892efe9238fc07c89cde71edb5d3a9541d83))
* revert dockerfile ([e735749](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e7357496d463e38fdbe95c7085586e103712b4c9))
* set Keycloak roles for consultants ([f824580](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f824580fb56b615a6d8d52a1b5ddf17a74cad720))


### Bug Fixes

* add alias message type of finished conversations to prevent deserialization exceptions ([6196ec3](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6196ec3a5ed52cffb010374c1683fb354f5870aa))
* add all consultants of agency to feedback group ([da683cd](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/da683cd9a8183d8d1906bf9efc5e9dc330261a0f))
* change testing consultant has not requested role ([5489735](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/548973510200f7eb1bec2132d41fb59574a41f30))
* prevent rocketchat operation for empty group id ([f239796](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f239796886539946bf6e50589b7c8d59d3f77c2a))
* prevent verification of already assigned consultants ([f97e6eb](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f97e6eb3e1cadbc8098fe49494983d96a5f834ec))
* rename role set key ([0313e66](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0313e66a6134accde883944b13aa04704c3eb9db))
* set has archive attribute also when consultant is team consultant ([2bb3e7e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2bb3e7e52294fdf7643b0da884da70497319ffb1))
* use collection utils for empty list ([7b5a741](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7b5a7412dfabcbf869f3ff74045948020d3214eb))
* use UTC in existing time calculation ([6e1662d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6e1662d4b40394614d7babe8c35acee2d5bf9b84))
* use UTC in time calculation ([ebe0bcf](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ebe0bcf9d569b1f3c05087019f101a73581912be))

### [2.12.1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.12.0...v2.12.1) (2021-10-27)

## [2.12.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.11.0...v2.12.0) (2021-10-27)


### Features

* added new field assign_date and trigger in session table ([576183f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/576183fa6b87bb0bc8c2c31681199360772a2c4b))
* added new flag "hasArchive" to user data response ([7b98852](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7b98852d31f1df86b6b2e6e750d080c3cc5e3d64))
* added unit test for RocketChatService ([65cbb28](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/65cbb283bbffa9e1a98f870169c24b8cd0c6cef3))
* added unit tests ([c99770b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c99770b669cd49367477ab79c76e52e9dfa7b09c))
* api endpoint for archived sessions and archived team sessions ([07b8c3b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/07b8c3b6ddbb43b4b5faee830af0d3fb4ad17ec6))
* api endpoint for archived sessions and archived team sessions ([9cb6fe4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9cb6fe444e4927f78242f904eaa6b5ebc77c20a1))
* api specification ([e64575c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e64575cbf483201cb438f7452d809c14e28222a5))
* extend admin api for consultant agency relation ([9e4dd52](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9e4dd52134007b27c1a1936bfa4eb87da7a14de1))
* extension of archiving / dearchiving ([6a060e1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6a060e13c45deafab6001dfc2bb9de6945b5c05c))
* extension of unit and integration tests ([9832d70](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9832d700bcaaac3757e9940c7c3596ffa5f9b436))
* extract async verification ([00784fe](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/00784fe5d93f090b65188a86512a264a43acf2d2))
* feature switch for new deletion workflows ([5f20ad6](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5f20ad6d1937e867992939da23bf744938e4cba9))
* fetch id from Rocket.Chat for user if only registered ([ab95acd](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ab95acd3307a76b463bd58e674db8a9aa936520b))
* first implementation of new delete workflow ([c71632e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c71632e5542a0ad9a219bf402d5083f9dae99a91))
* fix compiler errors ([8cc8cc7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8cc8cc73f47ad366649571c3d0ff60f3f1c148aa))
* fix some sonar issues ([023b9ba](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/023b9ba4fb5d5fea2993581709e633548f112a34))
* implementation of api endpoint and tests ([77aac1e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/77aac1ea67a76db8217d52965238d2f94d205a90))
* implementation of deletion workflow for registered only users ([3ac72ab](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3ac72abdd4f06c4033ac76ffd846f38acae2c24e))
* implementation of tests ([61cad18](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/61cad188086872772461f9e88a6ce21b52473cd2))
* increment max timeout ([00391c4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/00391c479a881d53348409e5db34b60263a32464))
* integration tests ([81ce54f](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/81ce54f8db78faeb98dc05e15e95d747b3b29ca3))
* javadoc extention ([3228a93](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3228a93fcec52150539b6b5917923c1e34caf390))
* optimization for edge case ([627c5ba](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/627c5bae2f6347b433f5c6c717b0057e17aa7794))
* optimizations, tests ([fdecea1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fdecea1f1ef172a7111ea573ebd8a3851f87f385))
* optimize amount of rocket chat user interactions ([51b7ff9](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/51b7ff9e2d9887a7abf1814d0d54dd6dd562fe3c))
* provide asynchronus removal of consultants ([8208335](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8208335a5daf41957586b14eda4bc58647df1c1e))
* remove redundant foreach iteration ([028b68c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/028b68ccbd84212e8d169b4c5c127f3afd588e0a))
* replace order ([53e945b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/53e945b7aaeee1836ca636b0ee6ebfd433c0dac9))
* restructure api to be hal conform ([c49ff3a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c49ff3a3ffab4f2fb29af2a5319de21fcbb69157))
* security config, integration and unit tests ([5952cb0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5952cb05c060ec4c50244ca8be8737e82a3db4bd))
* start adjusting documentation ([6c39f45](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6c39f4568ac5d2f9ea99d2676503d2e533d197c7))
* test for feature switches ([603a4c4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/603a4c495cfe402acd170f4767f1062618e199cc))
* unit and integration tests ([c563749](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c56374953f7523e6a1e94fbbe4f2179a9a38ff7c))
* unit and integration tests ([1bec4ef](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1bec4efb59edb6407a7d4d2223062c7da94c6667))
* updated service api descriptions ([e141980](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e1419800093f7511ad15de5ed189400b7bf2699e))


### Bug Fixes

* bugfix for npe ([7fa7358](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7fa73589124574043a650313a7b87c3467eb4afd))
* checkstyle error ([06d9292](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/06d9292c67a08febeea11e75bdb256c40b6b63fa))
* fixed unit test ([40dfba4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/40dfba47b04c0d77f3b378097f89a799a6c84f10))
* minor changes and filtering of group chats for new deletion workflow ([668f845](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/668f8457f3c089c8dbbaef752ec77a5d533ea589))
* minor changes and filtering of group chats for new deletion workflow ([74d36b6](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/74d36b659e48ee2009e6ffd51d1327a797d48790))
* missing import ([d56f5af](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d56f5afabe5bb57ac12a9ca195cd18639eac9264))
* order of archived sessions list ([33a1e37](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/33a1e370c2076cef953c1d1aecd30f711c4109f9))

## [2.11.0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/compare/v2.10.1...v2.11.0) (2021-06-18)


### Features

* adapt new keycloak version ([c487d52](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c487d526165b51d01c4ea184aaa22fbe57e4a63c))
* adapt new u25 monitoring settings ([29f6f71](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/29f6f71aa8b61095357412b5614cfdd3b2f1ea87))
* add deactivation workflow module ([1485c9b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1485c9b79f14a770c41141478710a4151826f490))
* add documentation for anonymous workflows ([a9f0799](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a9f07997452dd55e1fd4e40f3633b2c28508b947))
* add handling of deactivation when session has status new ([68fa7fc](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/68fa7fc46b6da90b1866211405b452794d6c0dbe))
* add integration test ([0e4a766](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0e4a766d9d2e3027aea008dbed8a721ef25df704))
* add integration test for DeletionSchedule ([bc64efa](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/bc64efabbcbf27e76411c1bd088f0cce5cb9ee4b))
* add scaffold for anonymous chat deletion cron job ([1dbb6b4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1dbb6b4116fb5818504a993839dc6bcf66c421c4))
* added action to send a finished conversation alias message ([18b7b07](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/18b7b0730405dd5110f30ae84156b84b437be1e7))
* added action to set rocket chat room read only ([9ea4a12](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9ea4a12558100192f339bc4b5deeacf5e0135417))
* added anonymous converswation info for users data ([1755e56](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1755e56da1c0ceb3774f0240b95257121783e57e))
* added call to create new anonymous enquiries ([4675f53](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4675f537df3e121825b949b6353cb3e92ba99439))
* added call to start a new anonymous conversation ([b10c9d2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b10c9d2eaa69549233b303bdf65cac9ac6c93d87))
* added check of anonymous enquiry consulting type setting value ([6a296fa](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6a296fae21fed3e962b07253ed42e64d4a16c338))
* added configurable allowed origins ([4971374](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4971374093b0c26a189181e2675ec5af225607e2))
* added controller for new anonymous conversation ([8a9104e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8a9104e508be9c6845e0f9f87c93b66752dc1798))
* added create date to anonymous session list ([6975323](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6975323dd6cb9e8ee8f84943665faebf2d6a60e5))
* added mapping for registration type ([d82fecb](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d82fecb322273147f36cfcac6961dc9ae91ba0d0))
* added property to consultingtypeservice.yaml ([fc7edb7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fc7edb7914e1a1380dd7c3376c589fd2489b33fa))
* configure csrf whitelist by properties ([5b25dcb](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5b25dcb2a9723203d18742a40cbd1fb1c77fd595))
* converted create date to iso date format ([9c5f1e0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9c5f1e01ae63bbbc2da7475001fe70758ecc2d80))
* correct caching config to be eh cache conform ([ec4039b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ec4039b2d392c777c2e07b6b3eb0dac73c3a487f))
* correct list order for anonymous sessions ([51224e8](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/51224e868ba97b8551834e2c1578d04bebf59c0f))
* define api to accept an anonymous enquiry ([a630668](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a6306687d05490c1274f723fe4098722acdd7085))
* enrich session with rocket chat data ([ea2b9a7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ea2b9a77ebd173699245d8546c38ed79629abd70))
* impl.getAllConsultingIds and tests ([1b139db](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1b139db726afc1138977510c85d6bac24d02d41c))
* implement anonymous user deletion cron ([ec43dda](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ec43dda41ccebca915f21912d9ee6d8b0a8bf66c))
* integrate live event trigger for assignment ([0e6491a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0e6491a1e5acc10ae102d693e972f40d21df971c))
* integrate permission for anonymous user role ([6850387](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/68503872958ba84a813f6c73bf2524d7e05a3a31))
* integrated list providers for anonymous and registered enquiries ([3186742](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3186742ce7fabcee93f24eec84f510e1ed66d2d7))
* merged develop ([9a2db41](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9a2db4128b6e37421d4471306700f5bd8c7a8b76))
* optimize session enrichment with rocket chat ([e2669dd](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e2669dd1844f821fff63f59cac9d663cbfb582ec))
* prevent add all consultants to rocket chat group for anonymous session ([6c58f70](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6c58f70c9b8a98dea1c6fe66a36021c9daaf3fd8))
* prevent double adding and removing of technical user ([7a0810b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7a0810b0803ef2c440ae9b953ef99cecfb0022a4))
* provide actions to deactivate session and keycloak user ([e1ef67b](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e1ef67b85ebc0b1854f6daaa6328f53082e23b2e))
* provide basic structure for conversation list handling ([c6a76ed](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c6a76edbd9400a22d463a473bdf75e93f3c6fccf))
* provide done sessions for active sesion request ([d1ecc9c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d1ecc9c93b02dd547338f0cdeb142a276b19c93a))
* provide new scheduler for deactivate group chat cron ([b50cdb4](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b50cdb4246de781cec3707bc3c456372f9e9b9ac))
* provide stop chat service using new stop chat action ([6f8b3eb](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/6f8b3ebc60cc1291dd66f8902e64567210c1766e))
* refactor assign session to reuse logic for assign anonymous enquiries ([4159304](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/41593044cb1cce1877e88224d0f5c42786f5dfcb))
* remove registry ids for anonymous users ([a63638c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/a63638ce9285d7d2437d141cd9a91c336248accb))
* removed getConsultingType endpoint ([08e765e](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/08e765ef3c442de05e8827034fadd1b32ba5bf3f))
* removed getConsultingType endpoint tests ([ec6b8de](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ec6b8de1ed99a2dc769672fdfdd1c212672d9b62))
* removed mocked object from test ([b8fca1c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b8fca1c78b0300895e20f53d79d4e4cd1c242e4f))
* removed unneccesary rc token header param ([0334cfb](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0334cfbb81068d8b4160d1f1559ccd39f0dd8ef2))
* removed unused yaml object ([2d1a2d5](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2d1a2d541e60672c521d0b34b5e3e54512835e54))
* resolve merge conflicts ([f11de51](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f11de51a83351ae6b1474950cc1b09dcfdbde9b7))
* return 204 on conversation endpoints ([7818acf](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/7818acf36eece9159655aabab61750fd6745ee97))
* reuse enrichment logic for sessions ([83f8cc0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/83f8cc04ca4f6a317908e6c8a1299b963456c229))
* specify api endpoint to finish an conversation ([eb591a1](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/eb591a1a350f0dcfeb206fb7d53ac2ad89424291))
* test fix ([ef2cd81](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ef2cd81000683ae21334ef16cd3111f4c5fe823d))
* trigger live event when finish anonymous session ([f3129b7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/f3129b7d0a7b4d1af0b8747edb146f94f4a7eb73))
* user new action infrastructure also for delete workflow actions ([31a4cb7](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/31a4cb7a27f3680b49b0723f9d9d555c8ba94541))


### Bug Fixes

* add rocket chat user id for generated anonymous user ([928bb18](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/928bb18d12e926a46a78c665be149cfbf996edc3))
* added ConsultingTypeService and refactored the ConsultingTypeSettings.java ([b663d5c](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b663d5c06b574b29bb5dce7527ec906a30ba47f6))
* added missing objects in yaml file ([8942ee0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/8942ee0af732cea90e7add936d50c1e91b640e88))
* changed != to notNull ([89953d3](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/89953d3d954c75c56076626bf3026e539ccc8d65))
* changed behavior of removing consultants from rocket chat groups ([4928604](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4928604ba67a93842e10383d9ef2fc25bc15f106))
* ConsultingTypePaginationLinksBuilder.java can be deleted ([b59e439](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/b59e4398dbae2818b8bde87cf3320d7d35f1dfdd))
* correct refresh expires in output ([fa3bfa2](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/fa3bfa20a3eec3c45e1f3fb4147a40d2a98a41c6))
* csrf filter test ([5fe6149](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/5fe61492a551aeea0975facf325d08a8d25d9a62))
* deleted function usage getAllConsultingIds ([c81db1d](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c81db1dd508f2ec72c4b8d9b6037c18eb1c8d32f))
* deleted unused files ([1fe25e6](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/1fe25e61d223aec9fbaa2b2b275b00fdbb1536f4))
* enabled workflows ([0fccb18](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/0fccb1875788394f8cd7b16d8067cdc29484053c))
* formatted ([83ba793](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/83ba793e766db8a8b5463af35f3089ac5702f4b7))
* formatted ([09f3e72](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/09f3e728ff8aaef6c338e822c907a8da641baf82))
* formatted ([9f63c38](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/9f63c386a07e64e7652a489a7eba5d8626ff2ef7))
* formatted code ([582ff7a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/582ff7a0bd3441b16e4257de7fc2f545965f7c07))
* formatted code, removed log ([2b08cae](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/2b08caeb38568cf0ea65a3996e437f506f683a36))
* impl. connection to new consulting type service and test refactoring ([e8eb156](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/e8eb156f957781701ddc3ad4e4969d2130a7428b))
* increased time to live ([57d0f15](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/57d0f15f1bbf3d6018fe068403eb3a584f3806cd))
* normalize offset item value to page value ([111663a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/111663a049e05b652504571e4dde3422ef2f3145))
* refactored ConsultingTypeManagerTest ([3f0764a](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3f0764a8061e75f44386cdf290bf36be3b0102a1))
* removed dev dependencies ([be8d008](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/be8d0084d55fce8814c4fca8633b10ab2cea0235))
* removed sout and unused yaml objects ([05f79f6](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/05f79f63d7a8870c89e66b0508c0b770e4335492))
* removed todo and used var ([c2ea3ba](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c2ea3bad0d7b3b33bb068f670b539500b73ee104))
* removed unnecessary line space ([3039c31](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/3039c3105a7f7a5829f38efa3ab3b982ce4ba527))
* removed unnecessary test ([4d651e5](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/4d651e5ab562b1feeab115429a9281ffcceae09d))
* renamed test function ([32e2d32](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/32e2d327da6e282795bf6b9297ef9b89a5bfe4f1))
* return only registered enquiries for registered enquiry endpoint ([c2b6196](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/c2b619601e2dd9085191252358831a907a5b10e1))
* tests for SessionToConsultantConditionProvider ([ea486a0](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/ea486a093cb834ef31da7137e992c97aa4e4e242))
* updated consultingtypeservice.yaml ([d05e8ca](https://github.com/CaritasDeutschland/caritas-onlineBeratung-userService/commit/d05e8ca73ccd7d01ce1e23cda5bda90453fc079d))

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
