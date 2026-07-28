const DEFAULT_DELAY_MS = 300;
const RANDOM_FAIL_RATE = 0; // 0 = never fail, set to 0.05 for 5% failure

function sleep(ms) {
  const delay = ms || DEFAULT_DELAY_MS + Math.random() * 300;
  return new Promise((resolve) => setTimeout(resolve, delay));
}

function shouldFail() {
  if (RANDOM_FAIL_RATE === 0) return false;
  return Math.random() < RANDOM_FAIL_RATE;
}

function applyFilters(records, filters) {
  if (!filters) return records;

  let result = [...records];

  // Text search across multiple fields
  if (filters.q) {
    const q = filters.q.toLowerCase();
    result = result.filter((r) =>
      Object.values(r).some(
        (val) => typeof val === 'string' && val.toLowerCase().includes(q)
      )
    );
  }

  // Exact field filters
  Object.entries(filters).forEach(([key, value]) => {
    if (key === 'q' || key === 'sortBy' || key === 'sortOrder' || key === 'page' || key === 'limit') return;
    if (value === undefined || value === null || value === '') return;
    result = result.filter((r) => String(r[key]) === String(value));
  });

  // Sorting
  if (filters.sortBy) {
    const order = filters.sortOrder === 'asc' ? 1 : -1;
    result.sort((a, b) => {
      const va = a[filters.sortBy];
      const vb = b[filters.sortBy];
      if (typeof va === 'string') return va.localeCompare(vb) * order;
      return ((va || 0) - (vb || 0)) * order;
    });
  }

  return result;
}

function paginate(records, page = 1, limit = 20) {
  const total = records.length;
  const totalPages = Math.max(1, Math.ceil(total / limit));
  const start = (page - 1) * limit;
  const items = records.slice(start, start + limit);
  return { items, total, page, limit, totalPages };
}

function generateId(prefix) {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
}

let dataStore = null;

export function setDataStore(store) {
  dataStore = store;
}

export const mockEngine = {
  async query(table, params = {}) {
    await sleep(params._noDelay ? 0 : undefined);
    if (shouldFail()) throw new Error(`Mock error: query ${table} failed`);

    if (!dataStore || !dataStore[table]) {
      return { items: [], total: 0, page: 1, limit: 20, totalPages: 0 };
    }

    const { page = 1, limit = 20, ...filters } = params;
    const filtered = applyFilters(dataStore[table], filters);
    return paginate(filtered, page, limit);
  },

  async get(table, id) {
    await sleep();
    if (shouldFail()) throw new Error(`Mock error: get ${table} failed`);

    if (!dataStore || !dataStore[table]) return null;
    return dataStore[table].find((r) => r.id === id) || null;
  },

  async create(table, data) {
    await sleep();
    if (shouldFail()) throw new Error(`Mock error: create ${table} failed`);

    if (!dataStore) throw new Error('Data store not initialized');

    const record = { id: generateId(table.slice(0, 2)), ...data };
    if (!dataStore[table]) dataStore[table] = [];
    dataStore[table].push(record);
    return record;
  },

  async update(table, id, data) {
    await sleep();
    if (shouldFail()) throw new Error(`Mock error: update ${table} failed`);

    if (!dataStore || !dataStore[table]) return null;
    const idx = dataStore[table].findIndex((r) => r.id === id);
    if (idx === -1) throw new Error(`Not found: ${table}.${id}`);

    dataStore[table][idx] = { ...dataStore[table][idx], ...data, id };
    return dataStore[table][idx];
  },

  async delete(table, id) {
    await sleep();
    if (shouldFail()) throw new Error(`Mock error: delete ${table} failed`);

    if (!dataStore || !dataStore[table]) return false;
    const idx = dataStore[table].findIndex((r) => r.id === id);
    if (idx === -1) return false;

    dataStore[table].splice(idx, 1);
    return true;
  },

  async search(table, criteria = {}) {
    await sleep();
    if (shouldFail()) throw new Error(`Mock error: search ${table} failed`);

    if (!dataStore || !dataStore[table]) return { items: [], total: 0 };

    let result = [...dataStore[table]];

    // Multi-field search
    if (criteria.q) {
      const q = criteria.q.toLowerCase();
      result = result.filter((r) =>
        Object.values(r).some(
          (val) => typeof val === 'string' && val.toLowerCase().includes(q)
        )
      );
    }

    // Array filters
    if (criteria.tags && criteria.tags.length > 0) {
      // Filter documents that have any of the specified tags
      result = result.filter((doc) => {
        const docTags = dataStore.documentTags?.filter((dt) => dt.documentId === doc.id) || [];
        return docTags.some((dt) => criteria.tags.includes(dt.tagId));
      });
    }

    // Type filter (array)
    if (criteria.types && criteria.types.length > 0) {
      result = result.filter((r) => criteria.types.includes(r.type));
    }

    // Status filter (array)
    if (criteria.statuses && criteria.statuses.length > 0) {
      result = result.filter((r) => criteria.statuses.includes(r.status));
    }

    return { items: result, total: result.length };
  },
};
