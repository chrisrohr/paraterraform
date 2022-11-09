import { describe, expect, it } from '@jest/globals';
import { installQuasarPlugin, qLayoutInjections } from '@quasar/quasar-app-extension-testing-unit-jest';
import { mount } from '@vue/test-utils';
import IndexPage from 'pages/IndexPage.vue';
import { DateTime } from 'luxon';

installQuasarPlugin();

describe('IndexPage', () => {
  const wrapper = mount(IndexPage,{
    global: {
      provide: qLayoutInjections(),
      mocks: {
        '$api': {
          get: async () => ({ data: []})
        }
      }
    },
  });

  it('mounts without errors', () => {
    expect(wrapper).toBeTruthy();
  });

  it('contains the required markup', () => {
    expect(wrapper.html()).toContain("Terraform States");
  });
});

describe('FormatDateRelative', () => {
  const page = mount(IndexPage, {
    global: {
      provide: qLayoutInjections(),
      mocks: {
        '$api': {
          get: async () => ({ data: []})
        }
      }
    },
  });

  it('make the date relative', () => {
    const initialTime = (Date.now()/1_000) - 10;

    const formattedTime = page.vm.formatDateRelative(initialTime);

    expect(formattedTime).toBe('10 seconds ago');
  });
});

describe('FormatDateISO', () => {
  const page = mount(IndexPage, {
    global: {
      provide: qLayoutInjections(),
      mocks: {
        '$api': {
          get: async () => ({ data: []})
        }
      }
    },
  });

  it('make the date in ISO format', () => {
    const now = Date.now();
    const initialTime = now/1000;

    const formattedTime = page.vm.formatDateISO(initialTime);

    const expected = DateTime.fromMillis(now).toISO();
    expect(formattedTime).toBe(expected);
  });
});

describe('ClassForDiffLine', () => {
  const page = mount(IndexPage, {
    global: {
      provide: qLayoutInjections(),
      mocks: {
        '$api': {
          get: async () => ({ data: []})
        }
      }
    },
  });

  it('should be green and bold when a new line', () => {
    const diff = [null, 'foo'];

    const style = page.vm.classForDiffLine(diff);

    expect(style).toBe('text-weight-bolder text-green-5');
  });

  it('should be red and bold when a removed line', () => {
    const diff = ['foo', null];

    const style = page.vm.classForDiffLine(diff);

    expect(style).toBe('text-weight-bolder text-red-10');
  });

  it('should be empty for regular change', () => {
    const diff = ['foo', 'bar'];

    const style = page.vm.classForDiffLine(diff);

    expect(style).toBe('');
  });
});

describe('FormatDiff', () => {
  const page = mount(IndexPage, {
    global: {
      provide: qLayoutInjections(),
      mocks: {
        '$api': {
          get: async () => ({ data: []})
        }
      }
    },
  });

  it('should add a plus when a new line', () => {
    const diff = [null, 'foo'];

    const style = page.vm.formatDiff(diff);

    expect(style).toBe('+ foo');
  });

  it('should add a minus when a removed line', () => {
    const diff = ['foo', null];

    const style = page.vm.formatDiff(diff);

    expect(style).toBe('- foo');
  });

  it('should show both values for a regular change', () => {
    const diff = ['foo', 'bar'];

    const style = page.vm.formatDiff(diff);

    expect(style).toBe('foo ---> bar');
  });
});

describe('TooltipForDiffLine', () => {
  const page = mount(IndexPage, {
    global: {
      provide: qLayoutInjections(),
      mocks: {
        '$api': {
          get: async () => ({ data: []})
        }
      }
    },
  });

  it('should indicate a new field when a new line', () => {
    const diff = [null, 'foo'];

    const style = page.vm.tooltipForDiffLine(diff);

    expect(style).toBe('Field has been added');
  });

  it('should indicate a removed field when a removed line', () => {
    const diff = ['foo', null];

    const style = page.vm.tooltipForDiffLine(diff);

    expect(style).toBe('Field has been removed');
  });

  it('should indicate field has changed for regular change', () => {
    const diff = ['foo', 'bar'];

    const style = page.vm.tooltipForDiffLine(diff);

    expect(style).toBe('Field has been changed');
  });
});

describe('ClassForTooltipForDiffLine', () => {
  const page = mount(IndexPage, {
    global: {
      provide: qLayoutInjections(),
      mocks: {
        '$api': {
          get: async () => ({ data: []})
        }
      }
    },
  });

  it('should be green when a new line', () => {
    const diff = [null, 'foo'];

    const style = page.vm.classForTooltipForDiffLine(diff);

    expect(style).toBe('bg-green-5');
  });

  it('should be red when a removed line', () => {
    const diff = ['foo', null];

    const style = page.vm.classForTooltipForDiffLine(diff);

    expect(style).toBe('bg-red-10');
  });

  it('should be empty for regular change', () => {
    const diff = ['foo', 'bar'];

    const style = page.vm.classForTooltipForDiffLine(diff);

    expect(style).toBe('');
  });
});

describe('RetrieveTerraformStates', () => {
  const page = mount(IndexPage, {
    global: {
      provide: qLayoutInjections(),
      mocks: {
        '$api': {
          get: async () => ({ data: [
              {
                "name": "paraterraformState",
              }
            ]})
        }
      }
    },
  });

  it('should request and store latest state data', () => {
    page.vm.retrieveTerraformStates();

    expect(page.vm.terraformStates).toEqual([
      {
        "name": "paraterraformState",
      }
    ]);
  });
});

describe('DisplayLatestDiff', () => {
  const page = mount(IndexPage, {
    global: {
      provide: qLayoutInjections(),
      mocks: {
        '$api': {
          get: async (a) => {
            if (a === '/states/latest') {
              return { data: [] };
            }

            return {
              data: {
                "name": ["paraterraformState", "newName"],
                "abc": ["foo", "bar"],
              }
            };
          }
        }
      }
    },
  });

  it('should request and store latest diff data', async () => {
    await page.vm.displayLatestDiff('foo');

    expect(page.vm.changes).toBe(true);
    expect(page.vm.diffChanges).toEqual({
        "abc": ["foo", "bar"],
        "name": ["paraterraformState", "newName"],
      });
  });
});

describe('ShowHistory', () => {
  const page = mount(IndexPage, {
    global: {
      provide: qLayoutInjections(),
      mocks: {
        '$api': {
          get: async () => ({ data: [
              {
                "name": "paraterraformState",
              }
            ]})
        }
      }
    },
  });

  it('should request and store history data', async () => {
    await page.vm.showHistory('foo');

    expect(page.vm.showHistoryDialog).toBe(true);
    expect(page.vm.stateHistoryData).toEqual([
      {
        "name": "paraterraformState",
      }
    ]);
  });
});

describe('ViewState', () => {
  const page = mount(IndexPage, {
    global: {
      provide: qLayoutInjections(),
      mocks: {
        '$api': {
          get: async (a) => {
            if (a === '/states/latest') {
              return { data: [] };
            }

            return {
              data: {
                "name": "paraterraformState",
              }
            };
          }
        }
      }
    },
  });

  it('should request and store current state', async () => {
    await page.vm.viewState('foo');

    expect(page.vm.viewStateDialog).toBe(true);
    expect(page.vm.currentStateContents).toEqual({
      "name": "paraterraformState",
    });
  });
});
