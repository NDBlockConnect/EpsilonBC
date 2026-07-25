import { Layers, Sparkles, Grid3x3, Puzzle } from "lucide-react";
import { FEATURES, type FeatureItem } from "@/data/site";
import { useReveal } from "@/hooks/useReveal";
import SectionLabel from "@/components/SectionLabel";

const ICON_MAP: Record<FeatureItem["icon"], typeof Layers> = {
  layers: Layers,
  sparkles: Sparkles,
  grid: Grid3x3,
  puzzle: Puzzle,
};

function FeatureRow({ feature }: { feature: FeatureItem }) {
  const { ref, visible } = useReveal();
  const Icon = ICON_MAP[feature.icon];

  return (
    <div
      ref={ref}
      className={`reveal group grid grid-cols-1 gap-6 border-t border-white/5 py-10 transition-all duration-700 md:grid-cols-12 md:items-start ${
        visible ? "is-visible" : ""
      }`}
    >
      {/* 序号 */}
      <div className="md:col-span-2">
        <span className="font-mono text-xs tracking-widest text-ink-faint transition-colors duration-500 group-hover:text-lumin">
          {feature.index}
        </span>
      </div>

      {/* 图标 + 标题 */}
      <div className="md:col-span-4">
        <div className="flex items-center gap-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg border border-white/8 text-lumin transition-all duration-500 group-hover:border-lumin/40 group-hover:shadow-[0_0_24px_rgba(94,234,212,0.2)]">
            <Icon size={18} />
          </div>
          <h3 className="font-sans text-xl font-semibold tracking-tight text-ink">
            {feature.title}
          </h3>
        </div>
      </div>

      {/* 描述 */}
      <div className="md:col-span-6">
        <p className="max-w-lg text-[15px] leading-relaxed text-ink-muted">
          {feature.description}
        </p>
      </div>
    </div>
  );
}

export default function Features() {
  const { ref, visible } = useReveal();

  return (
    <section id="features" className="relative py-28 sm:py-36">
      <div className="container">
        <div
          ref={ref}
          className={`reveal mb-20 ${visible ? "is-visible" : ""}`}
        >
          <SectionLabel index="01" title="核心特性" />
          <p className="mt-6 max-w-2xl text-base leading-relaxed text-ink-muted">
            EpsilonBC 延续自原 Epsilon 项目，由 BlockConnect
            社区维护。以下特性定义了客户端的技术骨架。
          </p>
        </div>

        <div>
          {FEATURES.map((feature) => (
            <FeatureRow key={feature.index} feature={feature} />
          ))}
          <div className="border-t border-white/5" />
        </div>
      </div>
    </section>
  );
}
