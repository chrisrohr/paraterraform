import { installQuasarPlugin, qLayoutInjections } from '@quasar/quasar-app-extension-testing-unit-jest';
import { describe, expect, it } from '@jest/globals';
import App from '../../../src/App.vue';
import { mount } from "@vue/test-utils";
import { createRouter, createWebHistory } from 'vue-router'
import routes from "../../../src/router/routes"

const router = createRouter({
  history: createWebHistory(),
  routes: routes,
})

installQuasarPlugin();

describe("App", () => {
  it("renders", () => {
    expect(typeof App.data).toBe('function')
  })

  it('mounts without errors', () => {
    const wrapper = mount(App,{
      global: {
        provide: qLayoutInjections()
      },
      plugins: [router],
    });
    expect(wrapper).toBeTruthy();
  });
})
