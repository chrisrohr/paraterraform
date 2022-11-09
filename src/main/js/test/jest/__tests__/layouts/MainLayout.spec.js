import { describe, expect, it } from '@jest/globals';
import { installQuasarPlugin, qLayoutInjections } from '@quasar/quasar-app-extension-testing-unit-jest';
import { mount } from '@vue/test-utils';
import MainLayout from "layouts/MainLayout";

installQuasarPlugin();

describe('MainLayout', () => {
  const wrapper = mount(MainLayout,{
    global: { provide: qLayoutInjections() },
  });

  it('mounts without errors', () => {
    expect(wrapper).toBeTruthy();
  });

  it('contains the title', () => {
    expect(wrapper.html()).toContain("Paraterraform");
  });

  it('contains the menu items', () => {
    expect(wrapper.html()).toContain("Home");
  });

  it('should allow for left drawer to be opened', () => {
    expect(wrapper.vm.leftDrawerOpen).toBe(true);
    wrapper.vm.toggleLeftDrawer();
    expect(wrapper.vm.leftDrawerOpen).toBe(false);
  });

  it('should allow for menu to be in mini state', () => {
    expect(wrapper.vm.miniState).toBe(true);
    wrapper.vm.toggleMiniState();
    expect(wrapper.vm.miniState).toBe(false);
  });
})
