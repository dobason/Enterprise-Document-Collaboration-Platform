import React, { memo, useEffect, useRef, useState } from 'react';

function AnimatedNumber({ value, duration = 1000 }) {
  const [display, setDisplay] = useState(0);
  const rafRef = useRef(null);
  const prevValueRef = useRef(0);

  useEffect(() => {
    if (value === prevValueRef.current) return;
    prevValueRef.current = value;

    const startValue = display;
    const startTime = performance.now();

    const animate = (now) => {
      const elapsed = now - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);

      setDisplay(Math.round(startValue + (value - startValue) * eased));

      if (progress < 1) {
        rafRef.current = requestAnimationFrame(animate);
      }
    };

    rafRef.current = requestAnimationFrame(animate);

    return () => {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
    };
  }, [value, duration]);

  return <span>{display.toLocaleString()}</span>;
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
