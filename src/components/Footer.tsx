import { SITE_META } from "@/data/site";

export default function Footer() {
  return (
    <footer className="relative border-t border-white/5 bg-void py-16">
      <div className="container">
        <div className="grid grid-cols-1 gap-12 md:grid-cols-12">
          {/* 品牌区 */}
          <div className="md:col-span-5">
            <div className="flex items-center gap-3">
              <span className="font-display text-3xl text-lumin glow-text">
                {SITE_META.symbol}
              </span>
              <div>
                <p className="font-sans text-sm font-semibold text-ink">
                  {SITE_META.name}
                </p>
                <p className="font-mono text-xs text-ink-faint">
                  {SITE_META.tagline}
                </p>
              </div>
            </div>
            <p className="mt-6 max-w-sm text-sm leading-relaxed text-ink-muted">
              {SITE_META.description}
            </p>
          </div>

          {/* 致谢区 */}
          <div className="md:col-span-4">
            <p className="font-mono text-xs uppercase tracking-widest text-ink-faint">
              Credits
            </p>
            <ul className="mt-4 space-y-2 text-sm text-ink-muted">
              <li>
                原项目：
                <a
                  href={SITE_META.upstreamUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-lumin transition-opacity hover:opacity-70"
                >
                  {SITE_META.upstreamName}
                </a>{" "}
                由 {SITE_META.upstreamAuthor}
              </li>
              <li>渲染底层：OpenLumin</li>
              <li>致谢：Meteor Client · Orbit · LeavesHack · TrollHack</li>
            </ul>
          </div>

          {/* 链接区 */}
          <div className="md:col-span-3">
            <p className="font-mono text-xs uppercase tracking-widest text-ink-faint">
              Links
            </p>
            <ul className="mt-4 space-y-2 text-sm">
              <li>
                <a
                  href={SITE_META.repoUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-ink-muted transition-colors hover:text-lumin"
                >
                  GitHub 仓库
                </a>
              </li>
              <li>
                <a
                  href={SITE_META.releasesUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-ink-muted transition-colors hover:text-lumin"
                >
                  Releases
                </a>
              </li>
              <li>
                <a
                  href={SITE_META.openLuminUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-ink-muted transition-colors hover:text-lumin"
                >
                  OpenLumin
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* 底部许可 */}
        <div className="mt-16 flex flex-col gap-4 border-t border-white/5 pt-8 text-xs text-ink-faint sm:flex-row sm:items-center sm:justify-between">
          <p className="font-mono leading-relaxed">
            {SITE_META.copyright}
          </p>
          <p className="font-mono">
            Licensed under {SITE_META.license}
          </p>
        </div>
      </div>
    </footer>
  );
}
