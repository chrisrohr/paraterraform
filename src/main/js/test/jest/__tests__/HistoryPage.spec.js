import { describe, expect, it } from '@jest/globals';
import { installQuasarPlugin, qLayoutInjections } from '@quasar/quasar-app-extension-testing-unit-jest';
import { mount } from '@vue/test-utils';
import HistoryPage from '../../../src/pages/HistoryPage.vue';

installQuasarPlugin();

describe('HistoryPage', () => {
  const wrapper = mount(HistoryPage,{
    global: { provide: qLayoutInjections() },
  });

  it('mounts without errors', () => {
    expect(wrapper).toBeTruthy();
  });

  it('contains the required markup', () => {
    expect(wrapper.html()).toContain("History Page");
  });
})
