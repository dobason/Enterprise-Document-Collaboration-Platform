// Helper to build Draft.js RawDraftContentState
function raw(blocks) {
  return JSON.stringify({ blocks, entityMap: {} });
}

function block(key, text, type = 'unstyled', inlineStyleRanges = [], depth = 0) {
  return { key, text, type, depth, inlineStyleRanges, entityRanges: [], data: {} };
}

function unstyled(key, text) {
  return block(key, text, 'unstyled');
}

function h1(key, text) {
  return block(key, text, 'header-one');
}

function h2(key, text) {
  return block(key, text, 'header-two');
}

function li(key, text, depth = 0) {
  return block(key, text, 'unordered-list-item', [], depth);
}

function oli(key, text, depth = 0) {
  return block(key, text, 'ordered-list-item', [], depth);
}

function bold(text) {
  return { offset: 0, length: text.length, style: 'BOLD' };
}

function italic(text) {
  return { offset: 0, length: text.length, style: 'ITALIC' };
}

function code(text) {
  return { offset: 0, length: text.length, style: 'CODE' };
}

function underline(text) {
  return { offset: 0, length: text.length, style: 'UNDERLINE' };
}

function highlight(text) {
  return { offset: 0, length: text.length, style: 'HIGHLIGHT' };
}

function mixedRanges(ranges) {
  const result = [];
  ranges.forEach(([style, text]) => {
    result.push({ offset: 0, length: text.length, style });
  });
  return result;
}

function mk(key, text, type = 'unstyled', styles = []) {
  // Combine all style ranges with correct offsets
  let offset = 0;
  const ranges = [];
  for (const [style, txt] of styles) {
    ranges.push({ offset, length: txt.length, style });
    offset += txt.length;
  }
  return { key, text, type, depth: 0, inlineStyleRanges: ranges, entityRanges: [], data: {} };
}

// --- Document contents ---

// d1: Q1 Engineering Report (APPROVED)
const d1_content = raw([
  h1('a1', 'Q1 2025 Engineering Report'),
  unstyled('a2', ''),
  mk('a3', 'Prepared by: Nguyen Van A', 'unstyled', [['BOLD', 'Prepared by: '], ['', 'Nguyen Van A']]),
  mk('a4', 'Date: March 31, 2025', 'unstyled', [['BOLD', 'Date: '], ['', 'March 31, 2025']]),
  unstyled('a5', ''),
  h2('a6', 'Executive Summary'),
  unstyled('a7', 'The engineering department has successfully completed 12 projects in Q1 2025, achieving a 94% on-time delivery rate. Key achievements include the deployment of the new API gateway, migration of legacy services to serverless architecture, and implementation of automated CI/CD pipelines.'),
  unstyled('a8', ''),
  h2('a9', 'Key Metrics'),
  li('a10', 'Projects completed: 12'),
  li('a11', 'On-time delivery: 94%'),
  li('a12', 'Code coverage: 87% (+5% vs Q4)'),
  li('a13', 'Incident response time: 12min (target: 15min)'),
  li('a14', 'System uptime: 99.97%'),
  unstyled('a15', ''),
  h2('a16', 'Department Highlights'),
  unstyled('a17', 'The platform team delivered the new microservices orchestration layer, reducing inter-service latency by 40%. The data team completed the migration from MongoDB to Aurora PostgreSQL, improving query performance by 3x. The frontend team launched the redesigned dashboard with real-time analytics.'),
  unstyled('a18', ''),
  mk('a19', 'Note: All metrics have been audited and verified by the operations team.', 'unstyled', [['BOLD', 'Note: '], ['', 'All metrics have been audited and verified by the operations team.']]),
]);

// d2: Employment Contract (APPROVED)
const d2_content = raw([
  h1('b1', 'Employment Contract'),
  unstyled('b2', ''),
  mk('b3', 'This Employment Contract (the "Agreement") is entered into as of March 1, 2025.', 'unstyled', [['BOLD', 'This Employment Contract'], ['', ' (the "Agreement") is entered into as of March 1, 2025.']]),
  unstyled('b4', ''),
  h2('b5', '1. Parties'),
  unstyled('b6', 'Between: EDMS Corporation, a company organized under the laws of Vietnam (the "Company")'),
  unstyled('b7', 'And: Nguyen Van A (the "Employee")'),
  unstyled('b8', ''),
  h2('b9', '2. Position and Duties'),
  unstyled('b10', 'The Employee shall serve as Senior Software Engineer, reporting to the Engineering Manager. The Employee\'s duties include:'),
  oli('b11', 'Designing and implementing software solutions'),
  oli('b12', 'Participating in code reviews and architectural discussions'),
  oli('b13', 'Mentoring junior team members'),
  oli('b14', 'Contributing to technical documentation'),
  unstyled('b15', ''),
  h2('b16', '3. Compensation'),
  mk('b17', 'Base Salary: VND 80,000,000 per month', 'unstyled', [['BOLD', 'Base Salary: '], ['', 'VND 80,000,000 per month']]),
  mk('b18', 'Stock Options: 5,000 stock units', 'unstyled', [['BOLD', 'Stock Options: '], ['', '5,000 stock units']]),
  mk('b19', 'Benefits: Health insurance, 15 days annual leave', 'unstyled', [['BOLD', 'Benefits: '], ['', 'Health insurance, 15 days annual leave']]),
  unstyled('b20', ''),
  h2('b21', '4. Term and Termination'),
  unstyled('b22', 'This Agreement shall commence on March 1, 2025 and continue until terminated by either party with 30 days written notice.'),
  unstyled('b23', ''),
  mk('b24', 'Signed this 28th day of February, 2025', 'unstyled', [['ITALIC', 'Signed this 28th day of February, 2025']]),
]);

// d3: HR Policy 2025 (PENDING)
const d3_content = raw([
  h1('c1', 'HR Policy 2025 — Employee Guidelines'),
  unstyled('c2', ''),
  mk('c3', 'Version 2.0 | Status: Pending Approval', 'unstyled', [['BOLD', 'Version 2.0'], ['', ' | Status: Pending Approval']]),
  unstyled('c4', ''),
  h2('c5', '1. Code of Conduct'),
  unstyled('c6', 'All employees are expected to maintain the highest standards of professional conduct. This includes treating colleagues with respect, maintaining confidentiality, and avoiding conflicts of interest.'),
  unstyled('c7', ''),
  h2('c8', '2. Remote Work Policy'),
  unstyled('c9', 'Employees may work remotely up to 3 days per week with manager approval. Remote work requires:'),
  li('c10', 'A stable internet connection (minimum 20 Mbps)'),
  li('c11', 'A dedicated workspace free from distractions'),
  li('c12', 'Availability during core hours (9:00 AM - 3:00 PM ICT)'),
  li('c13', 'Participation in daily stand-up meetings via video call'),
  unstyled('c14', ''),
  h2('c15', '3. Leave Policy'),
  unstyled('c16', 'Employees are entitled to:'),
  oli('c17', '15 days of annual leave per year'),
  oli('c18', '10 days of sick leave per year'),
  oli('c19', '5 days of personal leave per year'),
  oli('c20', 'Maternity/Paternity leave as per Vietnamese labor law'),
  unstyled('c21', ''),
  h2('c22', '4. IT Security'),
  mk('c23', 'All employees must complete the IT Security Awareness training within the first 30 days of employment. Password must be at least 12 characters and include uppercase, lowercase, numbers, and special characters.', 'unstyled', [['BOLD', 'All employees must complete the IT Security Awareness training'], ['', ' within the first 30 days of employment. Password must be at least 12 characters and include uppercase, lowercase, numbers, and special characters.']]),
]);

// d4: Monthly Sales Report - March (DRAFT)
const d4_content = raw([
  h1('d1', 'Monthly Sales Report — March 2025'),
  unstyled('d2', ''),
  mk('d3', 'Status: DRAFT — Not yet reviewed', 'unstyled', [['BOLD', 'Status: DRAFT'], ['', ' — Not yet reviewed']]),
  unstyled('d4', ''),
  h2('d5', 'Overview'),
  unstyled('d6', 'March showed strong performance across all regions. Total revenue reached VND 4.2 billion, exceeding targets by 12%.'),
  unstyled('d7', ''),
  h2('d8', 'Regional Breakdown'),
  unstyled('d9', ''),
  mk('d10', 'Northern Region: VND 1.8 billion (+15% vs target)', 'unstyled', [['BOLD', 'Northern Region: '], ['', 'VND 1.8 billion (+15% vs target)']]),
  mk('d11', 'Central Region: VND 0.9 billion (+8% vs target)', 'unstyled', [['BOLD', 'Central Region: '], ['', 'VND 0.9 billion (+8% vs target)']]),
  mk('d12', 'Southern Region: VND 1.5 billion (+11% vs target)', 'unstyled', [['BOLD', 'Southern Region: '], ['', 'VND 1.5 billion (+11% vs target)']]),
  unstyled('d13', ''),
  h2('d14', 'Top Products'),
  oli('d15', 'Enterprise Document Suite: VND 1.2 billion'),
  oli('d16', 'Cloud Storage Add-on: VND 0.8 billion'),
  oli('d17', 'API Integration Package: VND 0.6 billion'),
  unstyled('d18', ''),
  mk('d19', 'Note: Final numbers pending finance review.', 'unstyled', [['ITALIC', 'Note: '], ['ITALIC', 'Final numbers pending finance review.']]),
]);

// d5: NDA Agreement - Partner A (APPROVED)
const d5_content = raw([
  h1('e1', 'Non-Disclosure Agreement'),
  unstyled('e2', ''),
  mk('e3', 'Between EDMS Corporation and Partner A Corporation', 'unstyled', [['BOLD', 'Between EDMS Corporation and Partner A Corporation']]),
  unstyled('e4', ''),
  h2('e5', '1. Definition of Confidential Information'),
  unstyled('e6', 'Confidential Information shall include all data, materials, products, technology, software, specifications, and business strategies disclosed by one party to the other.'),
  unstyled('e7', ''),
  h2('e8', '2. Obligations'),
  unstyled('e9', 'The receiving party shall:'),
  li('e10', 'Maintain strict confidentiality of all protected information'),
  li('e11', 'Use the information only for the stated business purpose'),
  li('e12', 'Limit access to employees with a legitimate need'),
  li('e13', 'Return or destroy all materials upon request'),
  unstyled('e14', ''),
  h2('e15', '3. Term'),
  unstyled('e16', 'This Agreement shall remain in effect for 3 years from the date of signing.'),
  unstyled('e17', ''),
  h2('e18', '4. Governing Law'),
  unstyled('e19', 'This Agreement shall be governed by the laws of the Socialist Republic of Vietnam.'),
]);

// d6: Employee Handbook 2025 (PENDING)
const d6_content = raw([
  h1('f1', 'Employee Handbook 2025'),
  unstyled('f2', ''),
  mk('f3', 'Version 3.0 — Pending HR Director Approval', 'unstyled', [['BOLD', 'Version 3.0'], ['', ' — Pending HR Director Approval']]),
  unstyled('f4', ''),
  h2('f5', 'Welcome to EDMS'),
  unstyled('f6', 'Welcome to EDMS Corporation! We are thrilled to have you join our team. This handbook is designed to help you understand our culture, policies, and expectations.'),
  unstyled('f7', ''),
  h2('f8', 'Our Mission'),
  mk('f9', 'Empower organizations to manage documents securely, efficiently, and intelligently.', 'unstyled', [['ITALIC', 'Empower organizations to manage documents securely, efficiently, and intelligently.']]),
  unstyled('f10', ''),
  h2('f11', 'Work Hours'),
  unstyled('f12', 'Standard office hours are 8:30 AM to 5:30 PM, Monday through Friday. Flexible arrangements are available with manager approval.'),
  unstyled('f13', ''),
  h2('f14', 'Dress Code'),
  unstyled('f15', 'Business casual attire is expected. Casual Fridays are observed. Team events and client meetings may require formal attire.'),
]);

// d7: Q2 Budget Proposal (DRAFT)
const d7_content = raw([
  h1('g1', 'Q2 2025 Budget Proposal'),
  unstyled('g2', ''),
  mk('g3', 'Prepared by: Pham Thi D | Status: DRAFT', 'unstyled', [['BOLD', 'Prepared by: Pham Thi D'], ['', ' | Status: DRAFT']]),
  unstyled('g4', ''),
  h2('g5', 'Proposed Budget: VND 15.2 Billion'),
  unstyled('g6', 'The proposed budget for Q2 2025 reflects a 15% increase from Q1, driven by new headcount and infrastructure investments.'),
  unstyled('g7', ''),
  h2('g8', 'Breakdown'),
  mk('g9', 'Personnel: VND 8.5 billion (56%)', 'unstyled', [['BOLD', 'Personnel: '], ['', 'VND 8.5 billion (56%)']]),
  mk('g10', 'Infrastructure: VND 3.2 billion (21%)', 'unstyled', [['BOLD', 'Infrastructure: '], ['', 'VND 3.2 billion (21%)']]),
  mk('g11', 'Software & Tools: VND 1.8 billion (12%)', 'unstyled', [['BOLD', 'Software & Tools: '], ['', 'VND 1.8 billion (12%)']]),
  mk('g12', 'Training & Development: VND 0.9 billion (6%)', 'unstyled', [['BOLD', 'Training & Development: '], ['', 'VND 0.9 billion (6%)']]),
  mk('g13', 'Contingency: VND 0.8 billion (5%)', 'unstyled', [['BOLD', 'Contingency: '], ['', 'VND 0.8 billion (5%)']]),
  unstyled('g14', ''),
  h2('g15', 'Justification'),
  unstyled('g16', 'The increase is primarily due to the hiring of 5 new engineers (VND 2.1B) and the migration to AWS Graviton instances (VND 0.8B). These investments are expected to generate a 25% improvement in infrastructure cost efficiency.'),
]);

// d8: Vendor Agreement - XYZ Corp (DRAFT)
const d8_content = raw([
  h1('h1', 'Vendor Services Agreement'),
  unstyled('h2', ''),
  mk('h3', 'Between: EDMS Corporation and XYZ Corp', 'unstyled', [['BOLD', 'Between: '], ['', 'EDMS Corporation and XYZ Corp']]),
  unstyled('h4', ''),
  h2('h5', 'Services'),
  unstyled('h6', 'XYZ Corp shall provide cloud infrastructure hosting services including compute, storage, and networking.'),
  unstyled('h7', ''),
  h2('h8', 'Service Level Agreement'),
  li('h9', 'Uptime guarantee: 99.95%'),
  li('h10', 'Support response time: 30 minutes for critical issues'),
  li('h11', 'Monthly performance reports'),
  unstyled('h12', ''),
  h2('h13', 'Pricing'),
  mk('h14', 'Monthly fee: VND 450,000,000', 'unstyled', [['BOLD', 'Monthly fee: '], ['', 'VND 450,000,000']]),
  mk('h15', 'Term: 12 months', 'unstyled', [['BOLD', 'Term: '], ['', '12 months']]),
  mk('h16', 'Auto-renewal: 30-day notice required', 'unstyled', [['BOLD', 'Auto-renewal: '], ['', '30-day notice required']]),
  unstyled('h17', ''),
  mk('h18', 'DRAFT — Under legal review', 'unstyled', [['ITALIC', 'DRAFT — Under legal review']]),
]);

// d9: IT Security Policy (APPROVED)
const d9_content = raw([
  h1('i1', 'IT Security Policy'),
  unstyled('i2', ''),
  mk('i3', 'Version 2.0 | Approved: January 15, 2025', 'unstyled', [['BOLD', 'Version 2.0'], ['', ' | Approved: January 15, 2025']]),
  unstyled('i4', ''),
  h2('i5', '1. Password Requirements'),
  unstyled('i6', 'All employees must use passwords that meet the following criteria:'),
  li('i7', 'Minimum 12 characters'),
  li('i8', 'At least 1 uppercase letter'),
  li('i9', 'At least 1 lowercase letter'),
  li('i10', 'At least 1 number'),
  li('i11', 'At least 1 special character'),
  li('i12', 'Must be changed every 90 days'),
  unstyled('i13', ''),
  h2('i14', '2. Data Classification'),
  unstyled('i15', 'Data is classified into three tiers:'),
  mk('i16', 'Public: Information that can be freely shared', 'unstyled', [['BOLD', 'Public: '], ['', 'Information that can be freely shared']]),
  mk('i17', 'Internal: Information for internal use only', 'unstyled', [['BOLD', 'Internal: '], ['', 'Information for internal use only']]),
  mk('i18', 'Confidential: Sensitive information requiring encryption', 'unstyled', [['BOLD', 'Confidential: '], ['', 'Sensitive information requiring encryption']]),
  unstyled('i19', ''),
  h2('i20', '3. Access Control'),
  unstyled('i21', 'Access to systems and data is granted based on the principle of least privilege. All access must be reviewed quarterly.'),
  unstyled('i22', ''),
  h2('i23', '4. Incident Response'),
  unstyled('i24', 'Security incidents must be reported within 1 hour to the IT team. The incident response team will follow the NIST framework for containment, eradication, and recovery.'),
]);

// d10: Annual Review - Engineering Dept (APPROVED)
const d10_content = raw([
  h1('j1', 'Annual Performance Review — Engineering Department'),
  unstyled('j2', ''),
  mk('j3', 'Review Period: 2024 | Status: Approved', 'unstyled', [['BOLD', 'Review Period: 2024'], ['', ' | Status: Approved']]),
  unstyled('j4', ''),
  h2('j5', 'Overall Rating: 4.5 / 5.0 — Exceeds Expectations'),
  unstyled('j6', 'The Engineering Department has demonstrated exceptional performance throughout 2024. Under the leadership of the Engineering Manager, the team delivered all 47 planned projects and exceeded 92% of key performance indicators.'),
  unstyled('j7', ''),
  h2('j8', 'Key Achievements'),
  oli('j9', 'Successfully launched EDMS v2.0 with serverless architecture'),
  oli('j10', 'Reduced infrastructure costs by 35% through AWS optimization'),
  oli('j11', 'Achieved 99.99% system uptime'),
  oli('j12', 'Grew team from 12 to 18 engineers'),
  oli('j13', 'Published 15 technical blog posts'),
  unstyled('j14', ''),
  h2('j15', 'Areas for Improvement'),
  unstyled('j16', 'Documentation coverage needs improvement (currently at 72%, target is 85%). The department should allocate dedicated time for documentation in Q1 2025.'),
  unstyled('j17', ''),
  h2('j18', 'Goals for 2025'),
  li('j19', 'Achieve 90% code coverage across all services'),
  li('j20', 'Reduce deployment time by 50%'),
  li('j21', 'Implement AI-powered code review assistant'),
  li('j22', 'Obtain AWS Advanced Partner certification'),
]);

const now = new Date();
const day = (n) => {
  const d = new Date(now);
  d.setDate(d.getDate() - n);
  return d.toISOString();
};

export const seedData = {
  users: [
    { id: 'u1', email: 'owner@edms.vn', name: 'Nguyen Van A', role: 'OWNER', department: 'Engineering', avatar: null },
    { id: 'u2', email: 'editor@edms.vn', name: 'Tran Thi B', role: 'EDITOR', department: 'Engineering', avatar: null },
    { id: 'u3', email: 'viewer@edms.vn', name: 'Le Van C', role: 'VIEWER', department: 'HR', avatar: null },
    { id: 'u4', email: 'manager@edms.vn', name: 'Pham Thi D', role: 'MANAGER', department: 'Management', avatar: null },
    { id: 'u5', email: 'admin@edms.vn', name: 'Hoang Van E', role: 'ADMIN', department: 'Admin', avatar: null },
  ],

  folders: [
    { id: 'f1', name: 'Contracts', department: 'Engineering', ownerId: 'u1', createdAt: day(30) },
    { id: 'f2', name: 'HR Documents', department: 'HR', ownerId: 'u3', createdAt: day(25) },
    { id: 'f3', name: 'Reports', department: 'Management', ownerId: 'u4', createdAt: day(20) },
  ],

  tags: [
    { id: 't1', name: 'Urgent' },
    { id: 't2', name: 'Confidential' },
    { id: 't3', name: 'Draft' },
    { id: 't4', name: 'Final' },
    { id: 't5', name: 'Archived' },
  ],

  documents: [
    { id: 'd1', title: 'Q1 Engineering Report', type: 'Report', status: 'APPROVED', ownerId: 'u1', folderId: 'f3', content: d1_content, createdAt: day(14), updatedAt: day(10) },
    { id: 'd2', title: 'Employment Contract — Nguyen Van A', type: 'Contract', status: 'APPROVED', ownerId: 'u1', folderId: 'f1', content: d2_content, createdAt: day(20), updatedAt: day(15) },
    { id: 'd3', title: 'HR Policy 2025', type: 'Policy', status: 'PENDING', ownerId: 'u3', folderId: 'f2', content: d3_content, createdAt: day(7), updatedAt: day(5) },
    { id: 'd4', title: 'Monthly Sales Report — March', type: 'Report', status: 'DRAFT', ownerId: 'u2', folderId: 'f3', content: d4_content, createdAt: day(3), updatedAt: day(1) },
    { id: 'd5', title: 'NDA Agreement — Partner A', type: 'Contract', status: 'APPROVED', ownerId: 'u1', folderId: 'f1', content: d5_content, createdAt: day(30), updatedAt: day(28) },
    { id: 'd6', title: 'Employee Handbook 2025', type: 'Policy', status: 'PENDING', ownerId: 'u4', folderId: 'f2', content: d6_content, createdAt: day(10), updatedAt: day(8) },
    { id: 'd7', title: 'Q2 Budget Proposal', type: 'Report', status: 'DRAFT', ownerId: 'u4', folderId: 'f3', content: d7_content, createdAt: day(2), updatedAt: day(2) },
    { id: 'd8', title: 'Vendor Agreement — XYZ Corp', type: 'Contract', status: 'DRAFT', ownerId: 'u2', folderId: 'f1', content: d8_content, createdAt: day(1), updatedAt: day(0) },
    { id: 'd9', title: 'IT Security Policy', type: 'Policy', status: 'APPROVED', ownerId: 'u1', folderId: 'f2', content: d9_content, createdAt: day(60), updatedAt: day(55) },
    { id: 'd10', title: 'Annual Review — Engineering Dept', type: 'Report', status: 'APPROVED', ownerId: 'u1', folderId: 'f3', content: d10_content, createdAt: day(90), updatedAt: day(85) },
  ],

  versions: [
    { id: 'v1', documentId: 'd1', versionNumber: 1, content: raw([h1('a1','Q1 Engineering Report'), unstyled('a2','Initial draft of Q1 report covering January and February metrics.')]), createdBy: 'u1', createdAt: day(14) },
    { id: 'v2', documentId: 'd1', versionNumber: 2, content: raw([h1('a1','Q1 Engineering Report'), unstyled('a2','Updated with March data and executive summary.')]), createdBy: 'u1', createdAt: day(12) },
    { id: 'v3', documentId: 'd1', versionNumber: 3, content: d1_content, createdBy: 'u4', createdAt: day(10) },
    { id: 'v4', documentId: 'd2', versionNumber: 1, content: raw([h1('b1','Employment Contract'), unstyled('b2','Initial draft with base salary of VND 75,000,000.')]), createdBy: 'u1', createdAt: day(20) },
    { id: 'v5', documentId: 'd2', versionNumber: 2, content: d2_content, createdBy: 'u1', createdAt: day(15) },
    { id: 'v6', documentId: 'd3', versionNumber: 1, content: raw([h1('c1','HR Policy 2025'), unstyled('c2','Initial draft covering Code of Conduct and Leave Policy.')]), createdBy: 'u3', createdAt: day(7) },
    { id: 'v7', documentId: 'd3', versionNumber: 2, content: d3_content, createdBy: 'u3', createdAt: day(5) },
    { id: 'v8', documentId: 'd4', versionNumber: 1, content: d4_content, createdBy: 'u2', createdAt: day(3) },
    { id: 'v9', documentId: 'd5', versionNumber: 1, content: raw([h1('e1','NDA Agreement'), unstyled('e2','Standard NDA template with Partner A.')]), createdBy: 'u1', createdAt: day(30) },
    { id: 'v10', documentId: 'd5', versionNumber: 2, content: d5_content, createdBy: 'u1', createdAt: day(28) },
  ],

  permissions: [
    { id: 'p1', documentId: 'd1', userId: 'u1', role: 'OWNER' },
    { id: 'p2', documentId: 'd1', userId: 'u2', role: 'EDITOR' },
    { id: 'p3', documentId: 'd2', userId: 'u1', role: 'OWNER' },
    { id: 'p4', documentId: 'd2', userId: 'u3', role: 'VIEWER' },
    { id: 'p5', documentId: 'd3', userId: 'u3', role: 'OWNER' },
    { id: 'p6', documentId: 'd3', userId: 'u4', role: 'EDITOR' },
    { id: 'p7', documentId: 'd4', userId: 'u2', role: 'OWNER' },
    { id: 'p8', documentId: 'd4', userId: 'u1', role: 'EDITOR' },
    { id: 'p9', documentId: 'd5', userId: 'u1', role: 'OWNER' },
    { id: 'p10', documentId: 'd5', userId: 'u4', role: 'VIEWER' },
  ],

  documentTags: [
    { id: 'dt1', documentId: 'd1', tagId: 't4' },
    { id: 'dt2', documentId: 'd3', tagId: 't1' },
    { id: 'dt3', documentId: 'd3', tagId: 't2' },
    { id: 'dt4', documentId: 'd4', tagId: 't3' },
    { id: 'dt5', documentId: 'd7', tagId: 't3' },
    { id: 'dt6', documentId: 'd9', tagId: 't4' },
    { id: 'dt7', documentId: 'd10', tagId: 't4' },
  ],

  auditLogs: [
    { id: 'a1', documentId: 'd1', action: 'UPLOAD', performedBy: 'u1', timestamp: day(14) },
    { id: 'a2', documentId: 'd1', action: 'VIEW', performedBy: 'u2', timestamp: day(13) },
    { id: 'a3', documentId: 'd1', action: 'APPROVE', performedBy: 'u4', timestamp: day(10) },
    { id: 'a4', documentId: 'd3', action: 'UPLOAD', performedBy: 'u3', timestamp: day(7) },
    { id: 'a5', documentId: 'd3', action: 'SUBMIT', performedBy: 'u3', timestamp: day(6) },
  ],

  ocrResults: [
    { id: 'ocr1', documentId: 'd1', status: 'completed', text: 'Q1 Engineering Report\nPrepared by: Nguyen Van A\nDate: March 2025\n\nExecutive Summary:\nThe engineering department has completed 12 projects in Q1 2025, achieving a 94% on-time delivery rate. Key achievements include the deployment of the new API gateway, migration of legacy services to serverless architecture, and implementation of automated CI/CD pipelines.\n\nKey Metrics:\n- Projects completed: 12\n- On-time delivery: 94%\n- Code coverage: 87%\n- System uptime: 99.97%' },
    { id: 'ocr2', documentId: 'd9', status: 'completed', text: 'IT Security Policy\nVersion 2.0\n\n1. Password Requirements\nAll employees must use passwords with at least 12 characters including uppercase, lowercase, numbers, and special characters. Passwords must be changed every 90 days.\n\n2. Data Classification\nData is classified into three tiers: Public, Internal, and Confidential.\n\n3. Access Control\nLeast privilege principle with quarterly access reviews.' },
  ],

  shares: [],
  approvals: [],
};
