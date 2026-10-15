// k6 load test for GET /prices. Run with: k6 run -e VUS=50 -e DURATION=20s load-test.js
import http from 'k6/http';
import { check } from 'k6';

const VUS = Number(__ENV.VUS ?? 10);
const DURATION = __ENV.DURATION ?? '20s';
const BASE = __ENV.BASE_URL ?? 'http://localhost:8080';

export const options = {
    vus: VUS,
    duration: DURATION,
    summaryTrendStats: ['avg', 'p(50)', 'p(95)', 'p(99)', 'max'],
};

export default function () {
    const res = http.get(
        `${BASE}/prices?brandId=1&productId=35455&applicationDate=2020-06-14T16:00:00`
    );
    check(res, { 'status 200': (r) => r.status === 200 });
}
