# Deploy #

## satellite ##
```bash
 docker run --rm --name scorpio -d -p 80:80 -v /root/logs:/root/logs syncxplus/scorpio --spring.application.json='{"mws":{"mode":"satellite","app-name":"","app-version":"","marketplace":"","marketplace-id":"","marketplace-url":"","seller-id":"","access-key":"","secret-key":"","auth-token":""},"spring":{"datasource":{"url":"","username":"","password":"","initialization-mode":"never"}}}'
```
