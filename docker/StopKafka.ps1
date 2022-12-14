param (
    [Parameter(Mandatory = $false)]
    [switch] $RemoveVolumes
)

docker compose down (&{if ($RemoveVolumes) { '--volumes' }})