# 1️⃣ Build Stage: ใช้ Maven สร้าง Spring Boot JAR
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2️⃣ Run Stage: ใช้ OpenJDK รัน Spring Boot App
FROM openjdk:17-jdk-slim
WORKDIR /app

# 👇 เปลี่ยนชื่อไฟล์ JAR ให้ตรงกับของคุณ (ดูใน /target)
COPY --from=build /app/target/construction-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081

# ✅ ใช้พอร์ต 8081 เป็นค่าเริ่มต้น และให้ Render override ได้
ENTRYPOINT ["/bin/sh", "-c", "export FIXED_PORT=${PORT:-8081} && echo '🔥 Using PORT:' $FIXED_PORT && exec java -Dserver.port=$FIXED_PORT -Dserver.address=0.0.0.0 -jar app.jar"]