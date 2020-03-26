const fs = require('fs');
const https = require('follow-redirects').https;
const unzip = require('unzipper');


const DRUGBANK_USERNAME = process.env.DB_USER;
const DRUGBANK_PASSWORD = process.env.DB_PASS;

if (!DRUGBANK_USERNAME) {
    console.log("please set environment variable DB_USER")
    process.exit(1)
}

if (!DRUGBANK_PASSWORD) {
    console.log("please set environment variable DB_PASS")
    process.exit(1)
}

function download_zip(download_url, filename) {
    options = {
        "auth": `${DRUGBANK_USERNAME}:${DRUGBANK_PASSWORD}`
    };
    return new Promise((fulfill, reject) => https.get(download_url, options, (res) => {
        const { statusCode } = res;

        let error;
        if (statusCode !== 200) {
            // Consume response data to free up memory
            res.resume();

            reject(new Error(`Request Failed.\nStatus Code: ${statusCode}`));
            return;
        }

        let writeStream = fs.createWriteStream(filename);
        res.on('data', (d) => writeStream.write(d));

        res.on('end', () => {
            writeStream.close();
            fulfill(filename);
        });
   }));
}

async function each_drug_file(zip_path, callback) {
    const zip = fs.createReadStream(zip_path)
          .pipe(unzip.Parse({forceStream: true}));

    for await (const entry of zip) {
        let filename = entry.path;
        if (filename.endsWith(".json")) {
            entry.buffer().then((buffer) => {
                callback(JSON.parse(buffer.toString("utf8")));
            });
        } else {
            // free memory from this file
            entry.autodrain();
        }
    }
}


async function json_zip_example(download_url) {
    try {
        await download_zip(download_url, 'drugs.zip');
        await each_drug_file("drugs.zip", (drug_data) => {
            // TODO: do something awesome here!
            console.log(`${drug_data.drugbank_id}, ${drug_data.name}`);
        });
    } catch (err) {
        console.log(`failed to download and read zip file with error ${err}`);
    }
}

exports.download_zip = download_zip;
exports.each_drug_file = each_drug_file;
exports.json_zip_example = json_zip_example;
