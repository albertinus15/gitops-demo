FROM node:18-alpine

# Membuat direktori aplikasi
WORKDIR /usr/src/app

# Menginstal dependensi
# Menyalin package.json dan package-lock.json terlebih dahulu untuk memanfaatkan Docker cache
COPY package*.json ./
RUN npm ci --only=production

# Menyalin kode sumber aplikasi
COPY . .

# Mengekspos port yang digunakan aplikasi
EXPOSE 3000

# Mengatur variabel lingkungan
ENV NODE_ENV=production
ENV PORT=3000
ENV APP_VERSION=1.0.0

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -q -O - http://localhost:3000/health || exit 1

# Menjalankan aplikasi
CMD ["node", "app.js"]
