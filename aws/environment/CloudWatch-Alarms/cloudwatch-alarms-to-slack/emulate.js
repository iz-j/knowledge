var event = {
  awslogs: {
    data: 'H4sIAAAAAAAAAIWQy07DMBBFf6WKWDbEM34mu0oENjykNivaCjmxXSKlcUkcHkL9d1woC1Zs75wZnbmfyd6Oo97Z6uNgkyK5WlSLp7tytVrclMk88W+9HWIMBHNkiMgAY9z53c3gp0OcZPptzDq9r43OpkPntUmDT5vOT2a0emief/BVGKzeRx4JyAxIhjRbX9wuqnJVbXnDJKe5qyUBVtNcGZ6jc64WkmtLZTwxTvXYDO0htL6/brtghzEp1n/i/0223yrlq+3DafszaU00olQwLhlQLpXinHFkOSqFjFIuACgQgpLnUgiJHBUqELmiRESr0Mb2gt7HIoATJVHGilCo+W+r54dTICnSimDBacHwMiKPm+CYVABRs5Z5kwJYmerG8JTUzgrpjFMcNmFdLpcPy+1saUc/DY299+HaT70p3xv7/XcRRy9T9LBmNpyhWe/DzJ2wTZ8ct8cvCzD7gOcBAAA='
  }
};

var context = {
  invokeid: 'test',
  succeed: (message) => {
    console.log(message);
    return;
  },
  fail: (message) => {
    console.error(message);
    return;
  }
};

var lambda = require("./app");
lambda.handler(event, context);