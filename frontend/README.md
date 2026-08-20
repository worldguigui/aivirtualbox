# Virtual AI Box 前端

Vite + React 19 + TypeScript + TanStack Query。数据来自 Spring Boot 后端(`/api/dashboard`、`/step` 等),Maven 构建时自动编译并嵌入 jar。

## 开发(热更新)

```bash
npm install
npm run dev
```

Vite 运行在 `localhost:5173`,已配置代理把 `/api`、`/step` 等请求转发到后端 `localhost:8080`。

## 构建

```bash
npm run build   # 产物输出到 dist/
```

生产构建由 `mvn package` 中的 `frontend-maven-plugin` 自动执行,经 `maven-resources-plugin` 复制到 classpath 静态资源。

## 目录结构

```
src/
├─ api/          # fetch 封装与接口函数
├─ hooks/        # useDashboard(TanStack Query + Auto Run 循环)
├─ lib/          # 格式化/过滤工具
├─ components/   # BrandHeader / StatusPanel / SimulationControls / WorldMap / AgentList / EventList
├─ App.tsx       # 状态持有 + 键盘快捷键 + 布局
└─ types.ts      # 与后端 API 契约对应的类型定义
```
