import { describe, expect, it } from '@jest/globals';
import { installQuasarPlugin, qLayoutInjections } from '@quasar/quasar-app-extension-testing-unit-jest';
import { mount } from '@vue/test-utils';
import UploadPage from '../../../src/pages/UploadPage.vue';

installQuasarPlugin();

describe('UploadPage', () => {
  const wrapper = mount(UploadPage,{
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
    expect(wrapper.html()).toContain("Upload Page")
  });
})
