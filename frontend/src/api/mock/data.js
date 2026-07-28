import { seedData } from './seed';
import { setDataStore } from './engine';

// Deep clone the seed data so mutations don't affect the original
function clone(obj) {
  return JSON.parse(JSON.stringify(obj));
}

let store = null;

export function getDataStore() {
  if (!store) {
    store = clone(seedData);
    setDataStore(store);
  }
  return store;
}

export function resetDataStore() {
  store = clone(seedData);
  setDataStore(store);
  return store;
}

// Initialize on first import
getDataStore();

export default { getDataStore, resetDataStore };
