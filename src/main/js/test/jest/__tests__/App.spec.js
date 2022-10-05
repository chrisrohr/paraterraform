import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-jest';
import { describe, expect, it } from '@jest/globals';
import App from '../../../src/App.vue';

installQuasarPlugin();

describe("App", () => {
  it("renders", () => {
    expect(typeof App.data).toBe('function')
  })
})
