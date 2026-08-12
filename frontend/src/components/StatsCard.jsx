import React, { memo, useEffect, useRef, useState } from 'react';

function AnimatedNumber({ value }) {
  return <span>{Number(value || 0).toLocaleString()}</span>;
}

function StatsCardInner({ icon: Icon, label, value, color = 'primary' }) {
  const colorMap = {
    primary: 'bg-primary-50 text-primary-600',
    success: 'bg-green-50 text-green-600',
    warning: 'bg-amber-50 text-amber-600',
    info: 'bg-cyan-50 text-cyan-600',
  };

  const iconBg = colorMap[color] || colorMap.primary;

  return (
    <div className="card p-5">
      <div className="flex items-center gap-4">
        <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${iconBg}`}>
          <Icon size={24} />
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-2xl font-bold text-slate-800">
            <AnimatedNumber value={value} />
          </p>
          <p className="text-sm text-slate-500 mt-0.5">{label}</p>
        </div>
      </div>
    </div>
  );
}

const StatsCard = memo(StatsCardInner);
export default StatsCard;
