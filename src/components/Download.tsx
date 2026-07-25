import { useState } from "react";
import { Download as DownloadIcon, ExternalLink, GitBranch } from "lucide-react";
import { RELEASE_CHANNELS, SITE_META, type ReleaseChannel } from "@/data/site";
import { useReveal } from "@/hooks/useReveal";
import SectionLabel from "@/components/SectionLabel";
import { cn } from "@/lib/utils";

const STATUS_LABEL: Record<ReleaseChannel["status"], string> = {
  stable: "稳定",
  alpha: "Alpha",
  dev: "开发中",
};

const STATUS_STYLE: Record<ReleaseChannel["status"], string> = {
  stable: "border-lumin/40 text-lumin bg-lumin/5",
  alpha: "border-spectral/40 text-spectral bg-spectral/5",
  dev: "border-white/20 text-ink-muted bg-white/[0.03]",
};

function ReleaseCard({ channel }: { channel: ReleaseChannel }) {
  return (
    <div className="flex flex-col gap-6 border-t border-white/5 py-10">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <GitBranch size={18} className="text-lumin" />
          <div>
            <div className="flex items-center gap-3">
              <h3 className="font-sans text-xl font-semibold text-ink">
                {channel.version}
              </h3>
              <span
                className={cn(
                  "rounded-full border px-2.5 py-0.5 font-mono text-[10px] uppercase tracking-wider",
                  STATUS_STYLE[channel.status],
                )}
              >
                {STATUS_LABEL[channel.status]}
              </span>
            </div>
            <p className="mt-1 font-mono text-xs text-ink-faint">
              {channel.mcVersion} · 分支 {channel.branch}
            </p>
          </div>
        </div>
      </div>

      <p className="max-w-xl text-sm leading-relaxed text-ink-muted">
        {channel.description}
      </p>

      {/* 加载器标签 */}
      <div className="flex flex-wrap gap-2">
        {channel.loaders.map((loader) => (
          <span
            key={loader}
            className="rounded-md border border-white/8 bg-white/[0.02] px-3 py-1.5 font-mono text-xs text-ink-muted transition-colors duration-300 hover:border-lumin/30 hover:text-lumin"
          >
            {loader}
          </span>
        ))}
      </div>

      <a
        href={SITE_META.releasesUrl}
        target="_blank"
        rel="noopener noreferrer"
        className="group inline-flex w-fit items-center gap-2 rounded-full border border-white/10 px-5 py-2.5 text-sm text-ink-muted transition-all duration-300 hover:border-lumin/40 hover:text-lumin"
      >
        <DownloadIcon size={14} />
        前往 GitHub Releases
        <ExternalLink
          size={12}
          className="opacity-50 transition-opacity group-hover:opacity-100"
        />
      </a>
    </div>
  );
}

export default function Download() {
  const [activeIdx, setActiveIdx] = useState(0);
  const { ref, visible } = useReveal();
  const activeChannel = RELEASE_CHANNELS[activeIdx];

  return (
    <section id="download" className="relative py-28 sm:py-36">
      {/* 背景辉光 */}
      <div className="pointer-events-none absolute right-0 top-1/3 -z-10 h-[500px] w-[500px] rounded-full bg-[radial-gradient(circle,rgba(139,124,255,0.06)_0%,transparent_70%)]" />

      <div className="container">
        <div ref={ref} className={`reveal mb-16 ${visible ? "is-visible" : ""}`}>
          <SectionLabel index="04" title="下载" />
          <p className="mt-6 max-w-2xl text-base leading-relaxed text-ink-muted">
            选择适合的版本分支。所有构建均通过 GitHub Releases 发布，
            源码遵循 GPLv3 协议公开。
          </p>
        </div>

        {/* 版本切换 Tab */}
        <div className="mb-4 flex gap-1 border-b border-white/5">
          {RELEASE_CHANNELS.map((channel, idx) => (
            <button
              key={channel.branch}
              onClick={() => setActiveIdx(idx)}
              className={cn(
                "relative px-5 py-3 font-mono text-xs uppercase tracking-wider transition-colors duration-300",
                idx === activeIdx
                  ? "text-lumin"
                  : "text-ink-faint hover:text-ink-muted",
              )}
            >
              {channel.branch}
              {idx === activeIdx && (
                <span className="absolute inset-x-0 -bottom-px h-px lumin-line" />
              )}
            </button>
          ))}
        </div>

        <ReleaseCard channel={activeChannel} />

        <div className="mt-10 flex items-center gap-3 text-xs text-ink-faint">
          <span className="font-mono">build:</span>
          <code className="font-mono text-ink-muted">./gradlew build</code>
          <span className="h-3 w-px bg-white/10" />
          <code className="font-mono text-ink-muted">./gradlew runClient</code>
        </div>
      </div>
    </section>
  );
}
