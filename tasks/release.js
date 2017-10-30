'use strict';

const fs = require('fs');
const path = require('path');
const util = require('util');
const childProcess = require('child_process');
const semver = require('semver');
const Promise = require('bluebird');

const readFileAsync = Promise.promisify(fs.readFile);
const writeFileAsync = Promise.promisify(fs.writeFile);
const execAsync = Promise.promisify(childProcess.exec);

const WORKING_DIR = path.resolve(__dirname, '../');
const GRADLE_FILE = path.join(WORKING_DIR, 'chatz/build.gradle');
const VERSION_NAME_REGEX = /versionName '(\d+\.\d+\.\d+)'/;
const VERSION_NAME_FORMAT = 'versionName \'%s\'';

function exec(command, options) {
  return execAsync(command, options || {cwd: WORKING_DIR});
}

function buildDevelopment() {
  return Promise.coroutine(function*() {
    console.log('Building project...');
    yield exec('npm run build');
  })();
}

function updateVersion(versionType) {
  return Promise.coroutine(function*() {
    console.log('Updating version...');
    const projectGradle =  yield readFileAsync(GRADLE_FILE, 'utf8');
    const match = VERSION_NAME_REGEX.exec(projectGradle);
    if (!match) {
      throw new Error('Could not get project version');
    }
    const version = match[1];
    console.log(`  Current version is ${version}`);
    const nextVersion = semver.inc(version, versionType);
    console.log(`  Next version is ${nextVersion}`);
    const updatedProjectGradle = projectGradle.replace(match[0], util.format(VERSION_NAME_FORMAT, nextVersion));
    yield writeFileAsync(GRADLE_FILE, updatedProjectGradle);
    return nextVersion;
  })();
}

function commitFiles(version) {
  return Promise.coroutine(function*() {
    console.log('Committing files...');
    yield exec(`git commit -am "Release ${version}"`);
  })();
}

function createTag(version) {
  return Promise.coroutine(function*() {
    console.log(`Creating tag ${version}...`);
    yield exec(`git tag ${version}`);
  })();
}

function pushTag() {
  return Promise.coroutine(function*() {
    console.log('Pushing tag to remote...');
    yield exec('git push --tags');
  })();
}

// Run this if call directly from command line
if (require.main === module) {
  const versionType = process.argv[2];
  if (!versionType || ['major', 'minor', 'patch'].indexOf(versionType) === -1) {
    console.log('Usage:');
    console.log('npm run release major|minor|patch');
    process.exit(1);
  }
  Promise.coroutine(function*() {
    try {
      const version = yield updateVersion(versionType);
      console.log(`Releasing version ${version} to remote...`);
      yield buildDevelopment();
      yield commitFiles(version);
      yield createTag(version);
      yield pushTag();
      console.log(`Version ${version} released with success!`);
    } catch (err) {
      console.error(err);
      process.exit(1);
    }
  })();
}
