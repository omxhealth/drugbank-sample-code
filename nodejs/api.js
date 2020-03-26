const https = require('https');
const parseLinkHeader = require('parse-link-header');
const URL = require('url').URL;
const URLSearchParams = require('url').URLSearchParams;

const DRUGBANK_API = "https://api.drugbankplus.com/v1/";
const DRUGBANK_API_KEY = process.env.DB_API_KEY;

if (!DRUGBANK_API_KEY) {
    console.log("please set environment variable DB_API_KEY")
    process.exit(1)
}

const DRUGBANK_HEADERS = {
    "Authorization": DRUGBANK_API_KEY,
    "Content-Type": "application/json",
    "Accept": "application/json"
};

function drugbank_url(route, params) {
    const url = new URL(route, DRUGBANK_API);
    url.search = new URLSearchParams(params);
    return url;
}

class DBResponse {
    constructor(response, data) {
        this.response = response;
        this.data = data;
    }

    // pagination_next and total_count return information on requests
    // that offer pagination.
    // pagination_next returns an object like this:
    // {
    //     page: '2',
    //     per_page: '50',
    //     rel: 'next',
    //     url: 'https://api.drugbankplus.com/v1/drugs/DB00472/adverse_effects?page=2&per_page=50'
    // }
    get pagination_next() {
        let header = this.response.headers["link"];
        if (!header) return null;
        return parseLinkHeader(header).next;
    }

    // total_count returns the total number of items matched by the request.
    // this is only provided for paginated calls
    get total_count() {
        let header = this.response.headers["x-total-count"];
        if (!header) return null;
        return parseInt(header);
    }
}


/* makes a GET request to the DrugBank API.
   route : the url route
   params : url query parameters
   callback : a function which will be called with the parsed JSON result of the API call
   returns a DBResponse
   {
     data: JSON data returned from the call
     response: instance of http.ClientRequest
   }
 */
function drugbank_get(route, params) {
    let http_opts = {headers: DRUGBANK_HEADERS};
    let url = drugbank_url(route, params);

    return new Promise((fulfill, reject) =>
        https.get(url, http_opts, (res) => {
            const { statusCode } = res;

            if (statusCode !== 200) {
                // Consume response data to free up memory
                res.resume();
                reject(new Error(`Request Failed. Status Code: ${statusCode}`));
            }

            res.on('error', reject);

            // assemble and return JSON from
            res.setEncoding('utf8');
            let rawData = []
            res.on('data', (chunk) => rawData.push(chunk));
            res.on('end', () => {
                let data = JSON.parse(rawData.join(""));
                fulfill(new DBResponse(res, data));
            });
        }));
}

function pretty_log(result) {
    console.log(JSON.stringify(result.data, null, 4));
    return result;
}

function drug_names_example() {
    // drug_names request example
    drugbank_get("drug_names", {q: "tylenol"}).
        then(pretty_log,
             (err) => console.error(`Got error: ${err.message}`));
}


// drug-drug interaction (DDI) example. Gets interactions by
// drug ids
function ddi_example() {
    let drug_ids = ['DB01598', 'DB01597', 'DB12377', 'DB01004'];
    drugbank_get("ddi", {drugbank_id: drug_ids.join(',')}).
        then(pretty_log,
            (err) => console.error(`DDI failed: ${err.message}`));
}

async function adverse_effects_paging_example() {
    let page_1 = await drugbank_get('drugs/DB00472/adverse_effects');
    let page_2 = await drugbank_get(page_1.pagination_next.url);

    return page_1.data.concat(page_2.data);
}

exports.url = drugbank_url;
exports.Response = DBResponse;
exports.get = drugbank_get;
exports.drug_names_example = drug_names_example;
exports.ddi_example = ddi_example;
exports.adverse_effects_paging_example = adverse_effects_paging_example;
