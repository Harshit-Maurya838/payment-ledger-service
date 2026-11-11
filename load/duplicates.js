import http from 'k6/http';
import { check } from 'k6';
import exec from 'k6/execution';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const RUN_ID = __ENV.RUN_ID || 'run1';
const MERCHANT_ID = '11111111-1111-1111-1111-111111111111';

const created = new Counter('responses_201_created');
const replayed = new Counter('responses_200_replayed');
const inProgress = new Counter('responses_409_in_progress');

// 409 (request still in progress) is an expected answer under duplicate load, not a failure.
http.setResponseCallback(http.expectedStatuses(200, 201, 409));

export const options = {
    scenarios: {
        duplicates: {
            executor: 'shared-iterations',
            vus: 50,
            iterations: 5000,
            maxDuration: '5m',
        },
    },
    thresholds: {
        checks: ['rate==1'],
    },
};

export default function () {
    const i = exec.scenario.iterationInTest;
    const block = i - (i % 10);
    const position = i % 10;

    // Positions 0-6: fresh key. Positions 7-9: retry the key of positions 0-2 in the same block (30% reuse).
    const keyIndex = position >= 7 ? block + (position - 7) : i;

    const key = `${RUN_ID}-${keyIndex}`;
    // The body depends only on keyIndex, so a retry sends exactly the same bytes as the original.
    const body = JSON.stringify({
        merchantId: MERCHANT_ID,
        amount: 1000 + (keyIndex % 1000),
        currency: 'INR',
    });

    const res = http.post(`${BASE_URL}/payments`, body, {
        headers: { 'Content-Type': 'application/json', 'Idempotency-Key': key },
    });

    if (res.status === 201) created.add(1);
    else if (res.status === 200) replayed.add(1);
    else if (res.status === 409) inProgress.add(1);

    check(res, { 'status is 200, 201 or 409': (r) => [200, 201, 409].includes(r.status) });
}