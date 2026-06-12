param(
    [switch]$OnlyDatabase,
    [string]$ContainerName = 'desafio-pg-5433',
    [string]$DatabaseName = 'desafio_db',
    [string]$DbUser = 'ceiuser',
    [string]$DbPassword = 'ceipass',
    [int]$HostPort = 5433,
    [string]$MirrorContainerName = 'cei-digital-db-1'
)

$ErrorActionPreference = 'Stop'

function Assert-CommandExists {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Comando obrigatório não encontrado: $Name"
    }
}

function Wait-ForPostgres {
    param(
        [string]$Container,
        [string]$User,
        [string]$Password
    )

    for ($i = 0; $i -lt 30; $i++) {
        docker exec -e PGPASSWORD=$Password $Container pg_isready -U $User *> $null
        if ($LASTEXITCODE -eq 0) {
            return
        }
        Start-Sleep -Seconds 2
    }

    throw 'PostgreSQL não ficou pronto a tempo.'
}

function Ensure-Container {
    param(
        [string]$Container,
        [int]$Port,
        [string]$User,
        [string]$Password
    )

    $existing = docker ps -a --format "{{.Names}}" | Where-Object { $_ -eq $Container }
    if ($existing) {
        docker start $Container | Out-Null
    }
    else {
        docker run -d --name $Container -e POSTGRES_DB=postgres -e POSTGRES_USER=$User -e POSTGRES_PASSWORD=$Password -p ${Port}:5432 postgres:15 | Out-Null
    }

    Wait-ForPostgres -Container $Container -User $User -Password $Password
}

function Ensure-Database {
    param(
        [string]$Container,
        [string]$Database,
        [string]$User,
        [string]$Password
    )

    $exists = docker exec -e PGPASSWORD=$Password $Container psql -U $User -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$Database'"
    $existsValue = if ($null -eq $exists) { '' } else { $exists.ToString().Trim() }
    if ($existsValue -ne '1') {
        docker exec -e PGPASSWORD=$Password $Container psql -U $User -d postgres -c "CREATE DATABASE $Database OWNER $User;" | Out-Null
    }
}

function Ensure-Schema {
    param(
        [string]$Container,
        [string]$Database,
        [string]$User,
        [string]$Password,
        [string]$SchemaFilePath
    )

    $tables = docker exec -e PGPASSWORD=$Password $Container psql -U $User -d $Database -tAc "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN ('membros', 'projetos', 'projeto_membros') ORDER BY table_name"
    $current = if ($null -eq $tables) { '' } else { $tables.ToString() }

    if ($current -match 'membros' -and $current -match 'projetos' -and $current -match 'projeto_membros') {
        return
    }

    Get-Content -Raw $SchemaFilePath | docker exec -i -e PGPASSWORD=$Password $Container psql -U $User -d $Database | Out-Null
}

function Container-Exists {
    param([string]$Container)

    $existing = docker ps -a --format "{{.Names}}" | Where-Object { $_ -eq $Container }
    return $null -ne $existing
}

Assert-CommandExists -Name docker
Assert-CommandExists -Name mvn
Assert-CommandExists -Name java

Push-Location $PSScriptRoot
try {
    $schemaFilePath = Join-Path $PSScriptRoot 'schema-postgres.sql'

    Ensure-Container -Container $ContainerName -Port $HostPort -User $DbUser -Password $DbPassword
    Ensure-Database -Container $ContainerName -Database $DatabaseName -User $DbUser -Password $DbPassword
    Ensure-Schema -Container $ContainerName -Database $DatabaseName -User $DbUser -Password $DbPassword -SchemaFilePath $schemaFilePath

    if ($MirrorContainerName -and $MirrorContainerName -ne $ContainerName -and (Container-Exists -Container $MirrorContainerName)) {
        Ensure-Database -Container $MirrorContainerName -Database $DatabaseName -User $DbUser -Password $DbPassword
        Ensure-Schema -Container $MirrorContainerName -Database $DatabaseName -User $DbUser -Password $DbPassword -SchemaFilePath $schemaFilePath
    }

    Write-Host "PostgreSQL pronto em jdbc:postgresql://localhost:$HostPort/$DatabaseName com schema garantido"

    if ($OnlyDatabase) {
        return
    }

    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        throw 'Falha ao gerar o jar da aplicação.'
    }

    $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:$HostPort/$DatabaseName"
    $env:SPRING_DATASOURCE_USERNAME = $DbUser
    $env:SPRING_DATASOURCE_PASSWORD = $DbPassword

    java -jar .\target\backend-java-0.0.1-SNAPSHOT.jar
}
finally {
    Pop-Location
}
