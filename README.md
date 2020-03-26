# DrugBank Sample Code


This repo contains sample code to help you get started with the DrugBank API or your DrugBank portal downloads!

Be sure to check out the documentation:

 * [API V1 documentation](https://docs.drugbankplus.com/v1/)
 * [JSON documentation](https://docs.drugbankplus.com/json/)
 * [XML documentation](https://docs.drugbankplus.com/xml/)
 * [CSV documentation](https://docs.drugbankplus.com/csv/)
 
 
# Environment Variables

The sample code uses the following environment variables:

 * `DB_API_KEY` : the API key you use to make API calls, as seen at dev.drugbankplus.com
 * `DB_USER` : the username you use to sign in at portal.drugbankplus.com 
 * `DB_PASS` : the password you use to sign in at portal.drugbankplus.com 

# Scripts

## NodeJS

NodeJS examples can be found in the nodejs/ folder. These examples do have some external
dependencies, which are declared in the package.json file.

One way to experiment with the provided files is to write a script which imports and uses them.
All files export their functions and classes for import.

The following example scripts are provided:

 * `api.js`
    This script contains sample code for making requests to the API, in nodejs. 
 * `portal.js`
    This script contains sample code for downloading archives from your portal,
    and reading drug JSON files in a zip archive.
