import Navigation from "@/components/Navigation";
import Hero from "@/components/Hero";
import Features from "@/components/Features";
import Graphics from "@/components/Graphics";
import Addon from "@/components/Addon";
import Download from "@/components/Download";
import Community from "@/components/Community";
import Footer from "@/components/Footer";

export default function Home() {
  return (
    <div className="grain relative min-h-screen bg-void">
      <Navigation />
      <main>
        <Hero />
        <Features />
        <Graphics />
        <Addon />
        <Download />
        <Community />
      </main>
      <Footer />
    </div>
  );
}
