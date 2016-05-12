# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD 44 - Campus Alameda

José Semedo   78294 jose.francisco.semedo@gmail.com

Lídia Freitas 78559 lidiamcfreitas@gmail.com

João Marçal   78471 joao.marcal12@gmail.com


Repositório:
[tecnico-distsys/A_44-project](https://github.com/tecnico-distsys/A_44-project)

-------------------------------------------------------------------------------

## Instruções de instalação 


### Ambiente

[0] Iniciar sistema operativo

Linux


[1] Iniciar servidores de apoio

JUDDI:
```
O servidor de nomes a utilizar é o jUDDI (Java UDDI).
Para lançar o servidor, basta executar o seguinte comando na pasta juddi-.../bin:
 $./startup.sh (Linux e Mac)
 $./startup.bat (Windows)
```


[2] Criar pasta temporária

```
cd ~
mkdir Project
```


[3] Obter código fonte do projeto (versão entregue)

```
git clone  https://github.com/tecnico-distsys/A_44-project.git
git checkout tags/SD_R2
```


[4] Instalar módulos de bibliotecas auxiliares

```
cd uddi-naming
mvn clean install
No directório do projecto:
cd ws-handlers-aux
mvn clean install
cd ws-ca
mvn install exec:java (deixar a correr num terminal a parte)
cd ws-ca-cli
mvn install
cd ws-handlers
mvn install  (caso tenha removido as keys das respectivas pastas os testes vão falhar)
cd transporter-ws-cli
mvn install (caso a transporter-ws esteja configurada para usar handlers deverá adicionar a flag 
             -DskipTests caso contrário precisa de remover a pasta jaxws do src)

```

-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor**

```
cd transporter-ws
mvn clean install
mvn exec:java
```

[2] Construir **cliente** e executar testes
These tests require two transporters to be running at the same time and the server **MUST** not user security handlers
if you want to run this tests you **MUST remove/move the folder jaxws in src/** and **remove the anotation "@HandlerChain"**
**from the file TransporterPort.java in transporter-ws**

```
No Handlers:
cd transporter-ws-cli
mvn clean install
With Handlers:
cd transporter-ws-cli
mvn clean install -DskipTests
```



-------------------------------------------------------------------------------

### Serviço BROKER

[1] Construir e executar **servidor**
De modo a suportar replicação com um broker secundário é necessário:
```
cd broker-ws
mvn install exec:java -Dws.i=2 (numa janela de terminal)
cd broker-ws
mvn exec:java
```

Ou para apenas um broker (sem replicação):
```
cd broker-ws
mvn clean install
mvn exec:java
```


[2] Construir **cliente** e executar testes

```
cd broker-ws-cli
mvn clean install
```


-------------------------------------------------------------------------------

### Serviço CA

[1] Construir e executar **servidor**

```
cd ca-ws
mvn clean install exec:java
```

[2] Construir **cliente** e executar testes

```
cd ca-ws-cli
mvn clean install
```

-------------------------------------------------------------------------------
**FIM**
