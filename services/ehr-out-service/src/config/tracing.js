import { AsyncLocalStorage } from 'async_hooks';

const asyncLocalStorage = new AsyncLocalStorage();

console.log('Tracing initialised');

export const setCurrentSpanAttributes = attributes => {
  let store = asyncLocalStorage.getStore();
  if (!store) {
    store = {};
  }
  for (const [key, value] of Object.entries(attributes)) {
    store[key] = value;
  }
  asyncLocalStorage.enterWith(store);
};

export function getCurrentSpanAttributes() {
  return asyncLocalStorage.getStore();
}

export function startRequest(requestHandler) {
  asyncLocalStorage.run({}, () => {
    requestHandler();
  });
}
