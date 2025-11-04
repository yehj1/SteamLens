🎮 SteamLens

SteamLens — 开源的 Steam 游戏评论采集与分析工具
从官方公开接口抓取游戏评论，入库 PostgreSQL，并可扩展为语义洞察与趋势分析平台。

🚀 功能简介

基于 Steam 官方 JSON 接口 拉取评论；

入库 PostgreSQL；

提供 REST API 供检索；

可扩展 LLM 分析与多语言汇总。

🧩 API 接口
1️⃣ 拉取评论
POST /fetch/steam


示例请求：

curl -X POST http://localhost:8080/fetch/steam \
  -H "Content-Type: application/json" \
  -d '{"appId":"730","lang":"schinese","limit":200}'


参数说明：

字段	类型	说明
appId	string	Steam 游戏 ID（如 730 表示 CS2）
lang	string	评论语言（默认 schinese）
limit	int	拉取数量上限
dayRange	int?	可选，限制天数范围
2️⃣ 查询评论
GET /reviews?appId=730&lang=schinese&limit=50


返回数据库中最新的评论。

⚙️ 环境配置
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/steamlens
    username: steam
    password: steam


初始化数据库：

docker run -d --name pg-steam \
 -e POSTGRES_USER=steam -e POSTGRES_PASSWORD=steam -e POSTGRES_DB=steamlens \
 -p 5432:5432 postgres:15


启动项目：

mvn spring-boot:run

🧠 技术栈

Java 17

Spring Boot 3

PostgreSQL

Spring Web + Data JPA

Swagger OpenAPI

📈 后续扩展

接入大模型（LLM）进行评论主题与情绪总结

支持多语言评论分析

可视化游戏口碑趋势

📜 开源协议

本项目采用 Apache License 2.0 协议。
详情请见 LICENSE
。
