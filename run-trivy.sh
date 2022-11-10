rm report*.sarif
trivy fs --security-checks=config,vuln --severity=CRITICAL --format=sarif --output report.sarif .