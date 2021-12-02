#!/usr/bin/env python3

import os, requests

DRUGBANK_USERNAME = os.getenv("DB_USER")
if DRUGBANK_USERNAME is None: raise Exception("please set environment variable DB_USER")

DRUGBANK_PASSWORD = os.getenv("DB_PASS")
if DRUGBANK_PASSWORD is None: raise Exception("please set environment variable DB_PASS")


def download(export_name, filename):
    """
    downloads latest portal export with name export_name to filename.
    """

    url = "https://portal.drugbank.com/downloads/{}/latest".format(export_name)

    response = requests.request("GET", url,
            auth=(DRUGBANK_USERNAME, DRUGBANK_PASSWORD), # set basic auth
            stream=True) # stream data so that we can easily write response to file

    length = int(response.headers['content-length'])
    print("response status: {}".format(response.status_code))
    print("response data type: {}".format(response.headers['content-type']))
    print("response size: {}".format(length))

    # stream the file to disk.
    chunks_loaded = 0
    chunk_size = 1024
    with open(filename, 'wb') as fd:
        for chunk in response.iter_content(chunk_size=chunk_size):
            fd.write(chunk)
            chunks_loaded += 1
            if chunks_loaded % 4000 == 0:
                bytes_loaded = chunk_size * chunks_loaded
                print("saving progress={}%".format(bytes_loaded * 100 // length))

    print("file saved in {}".format(filename))
    return filename

if __name__ == '__main__':
    print("export name?")
    export_name = input()
    download(export_name, 'drugbank-download.zip')
