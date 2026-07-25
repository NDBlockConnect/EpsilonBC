import { Suspense, useRef, useMemo } from "react";
import { Canvas, useFrame, useThree } from "@react-three/fiber";
import { MeshDistortMaterial, Float, Icosahedron, Sparkles } from "@react-three/drei";
import { EffectComposer, Bloom, Vignette, Noise } from "@react-three/postprocessing";
import type { Mesh, Group } from "three";
import { MathUtils } from "three";

/**
 * 中央发光晶体：实心扭曲多面体 + 线框外壳
 * 随鼠标做轻微视差，持续自转。
 */
function EpsilonCrystal() {
  const group = useRef<Group>(null);
  const inner = useRef<Mesh>(null);
  const { pointer } = useThree();

  useFrame((_, delta) => {
    if (!group.current || !inner.current) return;
    group.current.rotation.y += delta * 0.18;
    group.current.rotation.x = MathUtils.lerp(
      group.current.rotation.x,
      pointer.y * 0.25,
      0.04,
    );
    group.current.rotation.z = MathUtils.lerp(
      group.current.rotation.z,
      -pointer.x * 0.15,
      0.04,
    );
    inner.current.rotation.x -= delta * 0.12;
    inner.current.rotation.z += delta * 0.06;
  });

  return (
    <group ref={group}>
      <Float speed={1.4} rotationIntensity={0.3} floatIntensity={0.6}>
        {/* 实心核心：扭曲发光体 */}
        <Icosahedron args={[1.35, 4]} ref={inner}>
          <MeshDistortMaterial
            color="#5EEAD4"
            emissive="#2DD4BF"
            emissiveIntensity={0.85}
            roughness={0.15}
            metalness={0.6}
            distort={0.32}
            speed={1.6}
          />
        </Icosahedron>

        {/* 外层线框壳 */}
        <Icosahedron args={[1.75, 1]}>
          <meshBasicMaterial
            color="#8B7CFF"
            wireframe
            transparent
            opacity={0.22}
          />
        </Icosahedron>

        {/* 外层呼吸光环 */}
        <mesh>
          <sphereGeometry args={[2.05, 32, 32]} />
          <meshBasicMaterial
            color="#5EEAD4"
            transparent
            opacity={0.04}
            side={1}
          />
        </mesh>
      </Float>
    </group>
  );
}

/**
 * 静态粒子尘：营造空间深度。
 */
function ParticleField() {
  const points = useRef<Group>(null);
  useFrame((_, delta) => {
    if (points.current) {
      points.current.rotation.y += delta * 0.02;
    }
  });
  return (
    <group ref={points}>
      <Sparkles
        count={120}
        scale={10}
        size={2.2}
        speed={0.3}
        opacity={0.6}
        color="#5EEAD4"
      />
      <Sparkles
        count={60}
        scale={8}
        size={1.4}
        speed={0.2}
        opacity={0.4}
        color="#8B7CFF"
      />
    </group>
  );
}

function SceneLights() {
  const lights = useMemo(
    () => (
      <>
        <ambientLight intensity={0.12} color="#8B7CFF" />
        <pointLight position={[4, 3, 5]} intensity={45} color="#5EEAD4" distance={20} />
        <pointLight position={[-5, -2, 3]} intensity={30} color="#8B7CFF" distance={18} />
        <pointLight position={[0, 5, -4]} intensity={20} color="#5EEAD4" distance={15} />
      </>
    ),
    [],
  );
  return lights;
}

interface LuminSceneProps {
  className?: string;
}

/**
 * 全屏 WebGL 画布：作为英雄区背景。
 * 低端设备会因 frameloop 与 dpr 限制自动降级。
 */
export default function LuminScene({ className }: LuminSceneProps) {
  return (
    <div className={className} aria-hidden="true">
      <Canvas
        camera={{ position: [0, 0, 5.2], fov: 45 }}
        dpr={[1, 2]}
        gl={{
          antialias: true,
          alpha: true,
          powerPreference: "high-performance",
        }}
        style={{ background: "transparent" }}
      >
        <Suspense fallback={null}>
          <SceneLights />
          <EpsilonCrystal />
          <ParticleField />
          <EffectComposer>
            <Bloom
              intensity={1.35}
              luminanceThreshold={0.15}
              luminanceSmoothing={0.9}
              mipmapBlur
              radius={0.8}
            />
            <Noise opacity={0.035} premultiply />
            <Vignette eskil={false} offset={0.25} darkness={0.85} />
          </EffectComposer>
        </Suspense>
      </Canvas>
    </div>
  );
}
