import pytest

from create_subscription import extract_subscription_id_from_headers


def test_extracts_subscription_id_from_headers():
    subscription_id = "00000000000000000000000000000005"
    headers = {'Location': f'https:local.nhs.uk/STU3/Subscription/{subscription_id}'}
    result = extract_subscription_id_from_headers(headers)
    assert result == subscription_id


def test_when_no_location_returns_empty_string():
    headers = {"MissingLocation": "someValue"}
    with pytest.raises(Exception):
        extract_subscription_id_from_headers(headers)

