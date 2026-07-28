import { getDataStore } from './mock/data';

export async function getDashboardStats() {
  const store = getDataStore();
  const docs = store.documents || [];
  const approvals = store.approvals || [];

  const totalDocuments = docs.length;
  const pendingApprovals = docs.filter((d) => d.status === 'PENDING').length;
  const approvedThisMonth = docs.filter((d) => {
    if (d.status !== 'APPROVED') return false;
    const updated = new Date(d.updatedAt);
    const now = new Date();
    return updated.getMonth() === now.getMonth() && updated.getFullYear() === now.getFullYear();
  }).length;

  // Documents by department
  const deptMap = {};
  docs.forEach((doc) => {
    const folder = store.folders?.find((f) => f.id === doc.folderId);
    const dept = folder?.department || 'Uncategorized';
    if (!deptMap[dept]) deptMap[dept] = 0;
    deptMap[dept]++;
  });

  const docsByDepartment = Object.entries(deptMap).map(([name, count]) => ({
    name,
    count,
  }));

  // Documents by status
  const statusMap = {};
  docs.forEach((doc) => {
    if (!statusMap[doc.status]) statusMap[doc.status] = 0;
    statusMap[doc.status]++;
  });

  const docsByStatus = Object.entries(statusMap).map(([status, count]) => ({
    status,
    count,
  }));

  return {
    totalDocuments,
    pendingApprovals,
    approvedThisMonth,
    docsByDepartment,
    docsByStatus,
    totalDepartments: new Set(docs.map((d) => {
      const folder = store.folders?.find((f) => f.id === d.folderId);
      return folder?.department || 'Uncategorized';
    })).size,
  };
}
