# Changelog

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

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
