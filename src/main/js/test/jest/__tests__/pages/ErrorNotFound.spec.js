import { describe, expect, it } from '@jest/globals';
import { installQuasarPlugin, qLayoutInjections } from '@quasar/quasar-app-extension-testing-unit-jest';
import { mount } from '@vue/test-utils';
import ErrorNotFound from 'pages/ErrorNotFound.vue';

installQuasarPlugin();

describe('ErrorNotFound', () => {
  const wrapper = mount(ErrorNotFound,{
    global: { provide: qLayoutInjections() },
  });

  it('mounts without errors', () => {
    expect(wrapper).toBeTruthy();
  });

  it('contains the required markup', () => {
    expect(wrapper.html()).toContain("Oops. Nothing here...");
  });
})
