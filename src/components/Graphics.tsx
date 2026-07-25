import { useReveal } from "@/hooks/useReveal";
import { GRAPHICS_CAPABILITIES, SITE_META } from "@/data/site";
import SectionLabel from "@/components/SectionLabel";

/**
 * 渲染管线可视化条：每个能力对应一条横向轨道，
 * 悬停时高亮并显示描述。
 */
function PipelineTrack({
  name,
  description,
  delay,
}: {
  name: string;
  description: string;
  delay: number;
}) {
  const { ref, visible } = useReveal();
  return (
    <div
      ref={ref}
      className={`reveal group flex flex-col gap-3 border-t border-white/5 py-6 transition-all duration-700 ${
        visible ? "is-visible" : ""
      }`}
      style={{ transitionDelay: `${delay}ms` }}
    >
      <div className="flex items-baseline justify-between gap-6">
        <div className="flex items-center gap-4">
          <span className="h-1.5 w-1.5 rounded-full bg-lumin/40 transition-all duration-500 group-hover:bg-lumin group-hover:shadow-[0_0_12px_rgba(94,234,212,0.6)]" />
          <span className="font-mono text-sm text-ink transition-colors duration-300 group-hover:text-lumin">
            {name}
          </span>
        </div>
        <span className="font-mono text-xs text-ink-faint">
          render.pass
        </span>
      </div>
      {/* 进度条 */}
      <div className="relative h-px w-full overflow-hidden bg-white/5">
        <span
          className={`absolute inset-y-0 left-0 lumin-line transition-all duration-1000 ${
            visible ? "w-full" : "w-0"
          }`}
          style={{ transitionDelay: `${delay + 200}ms` }}
        />
      </div>
      <p className="max-w-md text-sm leading-relaxed text-ink-muted opacity-0 transition-opacity duration-500 group-hover:opacity-100">
        {description}
      </p>
    </div>
  );
}

export default function Graphics() {
  const { ref, visible } = useReveal();

  return (
    <section id="graphics" className="relative py-28 sm:py-36">
      {/* 背景辉光 */}
      <div className="pointer-events-none absolute left-1/2 top-1/2 -z-10 h-[600px] w-[600px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-[radial-gradient(circle,rgba(94,234,212,0.06)_0%,transparent_70%)]" />

      <div className="container">
        <div className="grid grid-cols-1 gap-16 lg:grid-cols-12 lg:gap-12">
          {/* 左侧：标题与说明 */}
          <div className="lg:col-span-5">
            <div ref={ref} className={`reveal ${visible ? "is-visible" : ""}`}>
              <SectionLabel index="02" title="Lumin 图形系统" />
              <p className="mt-6 text-base leading-relaxed text-ink-muted">
                Lumin 渲染系统为客户端提供自定义渲染管线，
                统一抽象矩形、圆角矩形、阴影、模糊、TTF
                字体与纹理等基础图元，并按语义层合批调度。
              </p>
              <p className="mt-4 text-sm leading-relaxed text-ink-faint">
                底层能力已迁移至自研的 OpenLumin 项目，
                由 BlockConnect 团队持续演进。
              </p>
              <a
                href={SITE_META.openLuminUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="mt-8 inline-flex items-center gap-2 font-mono text-xs uppercase tracking-widest text-lumin transition-opacity hover:opacity-70"
              >
                <span className="h-px w-8 lumin-line" />
                OpenLumin 仓库
              </a>
            </div>
          </div>

          {/* 右侧：渲染管线轨道 */}
          <div className="lg:col-span-7">
            {GRAPHICS_CAPABILITIES.map((cap, idx) => (
              <PipelineTrack
                key={cap.name}
                name={cap.name}
                description={cap.description}
                delay={idx * 80}
              />
            ))}
            <div className="border-t border-white/5" />
          </div>
        </div>
      </div>
    </section>
  );
}
