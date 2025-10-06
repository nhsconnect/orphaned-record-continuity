const {
  extractDependencyUpdatesFromList,
  fromString,
  getAllUpdates,
  getAllUpdatesText,
  isUpdate
} = require('../parse-dependency-updates');

const multipleUpdatesRequired = [
  '/usr/local/Cellar/node/13.10.1/bin/node',
  '/Users/someone/git/prm-deductions-gp2gp-messenger/check_for_updates.js',
  '/Users/someone/git/prm-deductions-gp2gp-messenger:uuid@3.4.0:uuid@7.0.2:uuid@7.0.2',
  '/Users/someone/git/prm-deductions-gp2gp-messenger:ppc@3.4.0:ppc@5.0.2:ppc@7.0.2'
];

const testData = [
  '/usr/local/Cellar/node/13.10.1/bin/node',
  '/Users/someone/git/prm-deductions-gp2gp-messenger/check_for_updates.js',
  '/Users/someone/git/prm-deductions-gp2gp-messenger:uuid@3.4.0:uuid@7.0.2:uuid@7.0.2'
];

const noUpdatesRequired = [
  '/usr/local/Cellar/node/13.10.1/bin/node',
  '/Users/someone/git/prm-deductions-gp2gp-messenger/check_for_updates.js'
];

describe('parse_dependency_updates.js', () => {
  describe('extractDependencyUpdatesFromList', () => {
    it('should filter the list to just dependency updates', () => {
      expect(extractDependencyUpdatesFromList(testData)).toEqual([
        '/Users/someone/git/prm-deductions-gp2gp-messenger:uuid@3.4.0:uuid@7.0.2:uuid@7.0.2'
      ]);
    });

    it('should return empty string when no updates are required', () => {
      expect(extractDependencyUpdatesFromList(noUpdatesRequired)).toEqual([]);
    });
  });

  describe('isUpdate', () => {
    it('should return true if the string contains an update', () => {
      expect(
        isUpdate(
          '/Users/someone/git/prm-deductions-gp2gp-messenger:uuid@3.4.0:uuid@7.0.2:uuid@7.0.2'
        )
      ).toBe(true);
    });

    it('should return false if the string does not contain an update', () => {
      expect(isUpdate('/usr/local/Cellar/node/13.10.1/bin/node')).toBe(false);
    });
  });

  describe('fromString', () => {
    it('should convert the update string to an object', () => {
      expect(
        fromString(
          '/Users/someone/git/prm-deductions-gp2gp-messenger:uuid@3.4.0:uuid@7.0.2:uuid@7.0.2'
        )
      ).toEqual({
        package: 'uuid',
        wantedVersion: '3.4.0',
        currentVersion: '7.0.2',
        latestVersion: '7.0.2'
      });
    });
  });

  describe('getAllUpdates', () => {
    it('should return an array of objects', () => {
      expect(getAllUpdates(multipleUpdatesRequired)).toEqual([
        {
          package: 'uuid',
          wantedVersion: '3.4.0',
          currentVersion: '7.0.2',
          latestVersion: '7.0.2'
        },
        {
          package: 'ppc',
          wantedVersion: '3.4.0',
          currentVersion: '5.0.2',
          latestVersion: '7.0.2'
        }
      ]);
    });
  });

  describe('getAllUpdatesText', () => {
    it('should return a String', () => {
      expect(typeof getAllUpdatesText(testData)).toBe('string');
    });

    it('should return an empty string if no packages are passed in', () => {
      expect(getAllUpdatesText(noUpdatesRequired)).toEqual('');
    });

    it('should return html corresponding to the outdated package', () => {
      expect(getAllUpdatesText(testData)).toEqual(
        '<b><a href=https://www.npmjs.com/package/uuid>uuid</a></b>: 7.0.2 &rarr; 7.0.2<br>'
      );
    });

    it('should return html corresponding to the outdated packages', () => {
      expect(getAllUpdatesText(multipleUpdatesRequired)).toEqual(
        '<b><a href=https://www.npmjs.com/package/uuid>uuid</a></b>: 7.0.2 &rarr; 7.0.2<br><b><a href=https://www.npmjs.com/package/ppc>ppc</a></b>: 5.0.2 &rarr; 7.0.2<br>'
      );
    });
  });
});
