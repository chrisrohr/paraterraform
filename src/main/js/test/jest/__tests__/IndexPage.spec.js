import { describe, expect, it } from '@jest/globals';
import { installQuasarPlugin, qLayoutInjections } from '@quasar/quasar-app-extension-testing-unit-jest';
import { mount } from '@vue/test-utils';
import IndexPage from '../../../src/pages/IndexPage.vue';

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
})
