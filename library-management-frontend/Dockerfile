# 阶段1：构建阶段
FROM node:20-alpine AS build

# 设置工作目录
WORKDIR /app

# 配置npm使用淘宝镜像（内网环境优化）
RUN npm config set registry https://registry.npmmirror.com

# 复制package文件
COPY package*.json ./

# 安装依赖（使用npm install，因为可能没有package-lock.json）
RUN npm install

# 复制源代码
COPY . .

# 构建生产版本
RUN npm run build

# 阶段2：运行阶段（使用nginx提供静态文件服务）
FROM nginx:alpine

# 复制构建产物到nginx目录
COPY --from=build /app/dist /usr/share/nginx/html

# 复制nginx配置文件
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]

