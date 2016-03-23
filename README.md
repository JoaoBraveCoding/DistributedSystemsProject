# Projeto de Sistemas Distribuídos 2015-2016 #

Grupo de SD 44 - Campus Alameda

José Semedo   78294 jose.francisco.semedo@gmail.com

Lídia Freitas 78559 lidiamcfreitas@gmail.com

João Marçal   78471 joao.marcal12@gmail.com


Repositório:
[tecnico-distsys/C_XX-project](https://github.com/tecnico-distsys/C_XX-project/)

-------------------------------------------------------------------------------

## Instruções de instalação 


### Ambiente

[0] Iniciar sistema operativo

Indicar Windows ou Linux
Linux

[1] Iniciar servidores de apoio

JUDDI:
```
...
```


[2] Criar pasta temporária

```
cd ...
mkdir ...
```


[3] Obter código fonte do projeto (versão entregue)

```
git clone ... 
```
*(colocar aqui comandos git para obter a versão entregue a partir da tag e depois apagar esta linha)*


[4] Instalar módulos de bibliotecas auxiliares

```
cd uddi-naming
mvn clean install
```

```
cd ...
mvn clean install
```


-------------------------------------------------------------------------------

### Serviço TRANSPORTER

[1] Construir e executar **servidor**

```
cd ...-ws
mvn clean install
mvn exec:java
```

[2] Construir **cliente** e executar testes

```
cd ...-ws-cli
mvn clean install
```

...


-------------------------------------------------------------------------------

### Serviço BROKER

[1] Construir e executar **servidor**

```
cd ...-ws
mvn clean install
mvn exec:java
```


[2] Construir **cliente** e executar testes

```
cd ...-ws-cli
mvn clean install
```

...

-------------------------------------------------------------------------------
**FIM**
