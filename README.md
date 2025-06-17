# SSDD - Despliegue del proyecto

### PASOS PARA ARRANCAR TODOS LOS SERVICIOS

---

### 1. Inicializar Docker Swarm (si no está iniciado)

```bash
docker swarm init


--Construir las imagenes desde el directorio raiz,  Es obligatorio hacerlo antes de desplegar el stack, ya que se usan imágenes locales y no están en Docker Hub.--
docker build -t ssdd-db-mysql db-mysql
docker build -t ssdd-backend-rest backend-rest/es.um.sisdist.backend.Service
docker build -t ssdd-backend-grpc backend-grpc/es.um.sisdist.backend.grpc.GrpcServiceImpl
docker build -t ssdd-backend-rest-externo backend-rest-externo
docker build -t ssdd-frontend -f frontend/Dockerfile-devel frontend

--Desplegar el stack--
docker stack deploy -c docker-stack.yml ssdd

--Verificar que este funcionando--
docker service ls

A partir de aquí, puedes usar el frontend en:

http://127.0.0.1:5010/
y los backends en sus respectivos puertos.

Para lazar el TestClientPrueba y ver la funcionalidad del RestInterno de golpe:

mvn exec:java -pl backend-rest/es.um.sisdist.backend.Service -Dexec.mainClass="es.um.sisdist.backend.Service.TestClientPrueba"

Para lanzar script externo NO FUNCIONA CON EL SWARM, Dede la raiz del proyecto ejecutar:
Habria que hacer el mvn clean install -DskipTests y docker compose -f docker-compose-devel.yml up –build y ya se podria hacer:

sh scriptRestExterno.sh





