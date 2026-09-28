# Deploying SalesSavvy

SalesSavvy deploys as a Vercel frontend and a Railway Spring Boot API with Railway MySQL.
The repository must be pushed to GitHub before connecting either host.

## Railway API and database

1. Create a Railway project from the GitHub repository and add a MySQL service.
   Railway detects the root `Dockerfile` and `railway.json` for the API service.
2. Add these variables to the API service (replace `MySQL` if your database
   service has a different name):

   | Variable | Value |
   | --- | --- |
   | `DB_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
   | `DB_USERNAME` | `${{MySQL.MYSQLUSER}}` |
   | `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
   | `JWT_SECRET` | A unique random secret of at least 32 characters |
   | `SEED_DEMO_DATA` | `false` |
   | `ADMIN_EMAIL` | The store administrator's email |
   | `ADMIN_PASSWORD` | A strong password of at least 12 characters |
   | `SALESSAVVY_UPLOAD_DIR` | `/data/uploads` |

   Add optional integration secrets such as `OPENAI_API_KEY` and
   `RAZORPAY_KEY_SECRET` only through Railway's Variables page.
3. Generate a public domain for the API service. Attach a Railway volume to
   the API service mounted at `/data`; product image uploads are stored there.
4. The health check is `/api/health`.

## Vercel frontend

1. Import the same GitHub repository into Vercel. The root-level `vercel.json`
   configures the monorepo build and React Router fallback.
2. Add `VITE_API_BASE_URL` as the Railway API's public origin, without a path
   (for example, `https://your-api.up.railway.app`).
3. Deploy the frontend and copy its production URL.
4. Set Railway's `CORS_ALLOWED_ORIGINS` to that exact Vercel origin, including
   `https://` and no trailing slash. Redeploy the Railway API after changing it.

The frontend sends API calls and product-image requests to the configured API
origin. Keep all private credentials in Railway variables; never put them in
Vercel's `VITE_` variables or commit a local `.env` file.

## Rotating the administrator password

After signing in, open **Account security** in the admin sidebar to change the
administrator password. Use a new, unique password of at least 12 characters.
Changing a password invalidates all existing access tokens for that account.
`ADMIN_PASSWORD` is only used to bootstrap an admin when the database has no
users; changing that Railway variable does not reset an existing account.
