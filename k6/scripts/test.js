import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '10s', target: 30 },
        { duration: '20s', target: 30 },
        { duration: '1s', target: 0 },
    ],
    thresholds: {
        'http_req_duration': ['p(95)<200'],
        'http_req_failed': ['rate<0.01'],
    },
};

// Function to fetch a random item from an array
function getRandomItem(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

export function setup() {
    // Fetch all the category data
    const res = http.batch([
        ['GET', 'http://localhost:8084/categories/product-types'],
        ['GET', 'http://localhost:8084/categories/graphical-appearances'],
        ['GET', 'http://localhost:8084/categories/colour-groups'],
        ['GET', 'http://localhost:8084/categories/perceived-colour-values'],
        ['GET', 'http://localhost:8084/categories/perceived-colour-masters'],
        ['GET', 'http://localhost:8084/categories/departments'],
        ['GET', 'http://localhost:8084/categories/indices'],
        ['GET', 'http://localhost:8084/categories/index-groups'],
        ['GET', 'http://localhost:8084/categories/sections'],
        ['GET', 'http://localhost:8084/categories/garment-groups'],
        ['GET', 'http://localhost:8084/categories/product-groups'],
    ]);

    const productTypes = res[0].json();
    const graphicalAppearances = res[1].json();
    const colourGroups = res[2].json();
    const perceivedColourValues = res[3].json();
    const perceivedColourMasters = res[4].json();
    const departments = res[5].json();
    const indices = res[6].json();
    const indexGroups = res[7].json();
    const sections = res[8].json();
    const garmentGroups = res[9].json();
    const productGroups = res[10].json();

    return {
        productTypes,
        graphicalAppearances,
        colourGroups,
        perceivedColourValues,
        perceivedColourMasters,
        departments,
        indices,
        indexGroups,
        sections,
        garmentGroups,
        productGroups
    };
}


export default function (data) {
    const username = 'root';
    const password = 'root';

    // 1. Login to get JWT token
    const loginPayload = JSON.stringify({
        username: username,
        password: password,
    });

    const loginParams = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const loginRes = http.post('http://localhost:8081/auth/login', loginPayload, loginParams);

    check(loginRes, {
        'Login successful': (r) => r.status === 200,
    });

    if (loginRes.status !== 200) {
        console.error(`❌ Login failed for ${username}: ${loginRes.status} ${loginRes.body}`);
        return; // Stop execution if login fails
    }

    const accessToken = loginRes.json('accessToken');
    console.log(`✅ Login successful for ${username}`);

    const authHeaders = {
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        },
    };

    // 2. Create a new product
    const prodName = `ProdName-${__VU}-${__ITER}`;
    const createPayload = JSON.stringify({
        productCode: __VU, // Use VU number for unique product code
        prodName: prodName,
        detailDesc: `Description for ${prodName}`,
        productType: getRandomItem(data.productTypes),
        graphicalAppearance: getRandomItem(data.graphicalAppearances),
        colourGroup: getRandomItem(data.colourGroups),
        perceivedColourValue: getRandomItem(data.perceivedColourValues),
        perceivedColourMaster: getRandomItem(data.perceivedColourMasters),
        department: getRandomItem(data.departments),
        index: getRandomItem(data.indices),
        indexGroup: getRandomItem(data.indexGroups),
        section: getRandomItem(data.sections),
        garmentGroup: getRandomItem(data.garmentGroups),
        productGroup: getRandomItem(data.productGroups)
    });

    const createRes = http.post('http://localhost:8084/products', createPayload, authHeaders);

    check(createRes, {
        'Product creation successful': (r) => r.status === 201,
    });

    let productId;
    if (createRes.status === 201) {
        // In Spring Data JPA, the returned object from save has the ID.
        // Let's assume the response body is the created product.
        productId = createRes.json('productId');
        console.log(`✅ Product created successfully: ${productId}`);
    } else {
        console.error(`❌ Product creation failed: ${createRes.status} ${createRes.body}`);
        return;
    }
    sleep(1);

    // 3. Read the product
    const readRes = http.get(`http://localhost:8084/products/${productId}`, authHeaders);

    check(readRes, {
        'Product read successful': (r) => r.status === 200,
        'Product name is correct': (r) => r.json('prodName') === prodName,
    });

    if (readRes.status === 200) {
        console.log(`✅ Product read successfully: ${productId}`);
    } else {
        console.error(`❌ Product read failed: ${readRes.status} ${readRes.body}`);
    }
    sleep(1);

    // 4. Update the product
    const updatedProdName = `Updated-${prodName}`;
    const updatePayload = JSON.stringify({
        productCode: __VU + 1000, // new product code
        prodName: updatedProdName,
        detailDesc: `Updated description for ${prodName}`,
        productType: getRandomItem(data.productTypes),
        graphicalAppearance: getRandomItem(data.graphicalAppearances),
        colourGroup: getRandomItem(data.colourGroups),
        perceivedColourValue: getRandomItem(data.perceivedColourValues),
        perceivedColourMaster: getRandomItem(data.perceivedColourMasters),
        department: getRandomItem(data.departments),
        index: getRandomItem(data.indices),
        indexGroup: getRandomItem(data.indexGroups),
        section: getRandomItem(data.sections),
        garmentGroup: getRandomItem(data.garmentGroups),
        productGroup: getRandomItem(data.productGroups)
    });

    const updateRes = http.put(`http://localhost:8084/products/${productId}`, updatePayload, authHeaders);

    check(updateRes, {
        'Product update successful': (r) => r.status === 200,
        'Updated product name is correct': (r) => r.json('prodName') === updatedProdName,
    });

    if (updateRes.status === 200) {
        console.log(`✅ Product updated successfully: ${productId}`);
    } else {
        console.error(`❌ Product update failed: ${updateRes.status} ${updateRes.body}`);
    }
    sleep(1);


}
