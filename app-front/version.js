/*
 * Set project version to 'environments/version.ts'
 */
const fs = require('fs');
const path = require('path');

console.log(`Detect version from pom.xml`);
let pomXml = fs.readFileSync(path.join(__dirname, '../pom.xml'));
let versionMatch = pomXml.toString().match(new RegExp('(<version>).*(</version>)'));
let version = versionMatch[0].replace(versionMatch[1], '').replace(versionMatch[2], '');
console.log(`Version detected! -> ${version}`);

console.log(`Output to version.ts`);
fs.writeFileSync(path.join(__dirname, 'src/environments/version.ts'), `export const APP_VERSION = '${version}';`);

console.log(`Finished!`);