// Usage: node parse_updates.js $(npm outdated --parseable)

const updateRegexPattern = '^.*:(.*)@(.*):.*@(.*):.*@(.*)$';

const extractDependencyUpdatesFromList = listOfUpdates =>
  listOfUpdates.filter(item => isUpdate(item));

const isUpdate = updateString => updateString.split(':').length > 1;

const fromString = updateString => {
  const matcher = updateString.match(updateRegexPattern);

  return {
    package: matcher[1],
    wantedVersion: matcher[2],
    currentVersion: matcher[3],
    latestVersion: matcher[4]
  };
};

const getAllUpdates = listOfUpdates =>
  extractDependencyUpdatesFromList(listOfUpdates).map(update => fromString(update));

const getAllUpdatesText = listOfUpdates => {
  const allUpdates = getAllUpdates(listOfUpdates);
  return allUpdates.reduce(
    (acc, item) =>
      acc +
      `<b><a href=https://www.npmjs.com/package/${item.package}>${item.package}</a></b>: ${item.currentVersion} &rarr; ${item.latestVersion}<br>`,
    ''
  );
};

module.exports = {
  extractDependencyUpdatesFromList,
  isUpdate,
  fromString,
  getAllUpdates,
  getAllUpdatesText
};

console.log(getAllUpdates(process.argv));
