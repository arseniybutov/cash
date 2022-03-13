#define LINUX
#define BUFFER_SIZE 150

#include <iostream>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <resolv.h>
#include <string.h>
#include <unistd.h> 
#include <termios.h>
#include <stdlib.h>

#include <stdio.h>

#include <fcntl.h>   /* ���������� ���������� ������� */
//#include <errno.h>   /* ���������� ����� ������ */
//#include <stdlib.h>
//#include <time.h>
//#include <string.h>
//#include <sstream>

#include "gui_structs.h"

using namespace std;

long DRIVER_VERSION = 1004;
int DEBUGLEVEL = 2;
char ipAddress[16];
int ipPortNum;

extern "C" int ScreenShow(ScreenParams* pScreenParams);
extern "C" void ScreenClose();

int loadConfig(char *pIPAddr, int *pIPPort) {
	char tmpBuf[BUFFER_SIZE];
	char *tmpCh = &tmpBuf[0];

	FILE *fileID;
	fileID = fopen("./banklib.ini", "r+");
	if (fileID == NULL)
		return 1;
	int err = 0;

	if (fscanf(fileID, "%s %s", tmpCh, pIPAddr) != 2)
		err++;
	if (fscanf(fileID, "%s %d", tmpCh, pIPPort) != 2)
		err++;
	if (fscanf(fileID, "%s %d", tmpCh, &DEBUGLEVEL) != 2)
		err++;
	fclose(fileID);
	return err;
}

int saveToFile(char *dataToSave) {
	if (DEBUGLEVEL < 1)
		return 0;
	long writtenBytes;
	long lengthToSave;
	const char *fileName = "./banklib.log";

	//if ((DEBUGLEVEL & 1) == 1)
	//	printf("%s", dataToSave);
	if ((DEBUGLEVEL & 2) == 2) {
		int fileDesc;
		fileDesc = open(fileName, O_APPEND | O_RDWR);
		if (fileDesc <= 0) {
			fileDesc = creat(fileName, O_CREAT | O_RDWR);
		}
		if (fileDesc <= 0) {
			return 1100;
		}

		lengthToSave = strlen(dataToSave);
		writtenBytes = write(fileDesc, dataToSave, strlen(dataToSave));
		close(fileDesc);
		if (lengthToSave != writtenBytes)
			return 1103;
	}
	return 0;
}

int connectToCash(char *host_out, int port_out, char* mass, int ID) {
	int sd_out;
	struct sockaddr_in dest_out;

	int ssize = 0;
	int bytes_send, bytes_read;
	int err = 5;
	char buffer[BUFFER_SIZE];
	char *buf = &buffer[0];
	if (DEBUGLEVEL > 0) saveToFile("\nconnectToCash Started 1");
	bzero(&dest_out, sizeof(dest_out));
	dest_out.sin_family = AF_INET;
	dest_out.sin_port = htons(port_out);
	inet_aton(host_out, &dest_out.sin_addr);
	if (DEBUGLEVEL > 0) saveToFile("\nconnectToCash Started 2");
	ssize = strlen(mass);

	if (DEBUGLEVEL > 0)
		saveToFile(mass);

	sd_out = socket(PF_INET, SOCK_STREAM, 0);
	if (sd_out >= 0) {
		if (DEBUGLEVEL > 0) {
			saveToFile("\nSocket is created\n");
		}

		err = connect(sd_out, (struct sockaddr *) &dest_out, sizeof(dest_out));
		if (err == 0) {
			if (DEBUGLEVEL > 0) {
				saveToFile("Connected to Cash\n");
			}
			bytes_send = send(sd_out, mass, ssize, 0);
			if (bytes_send != ssize)
				err = 200;
			if (DEBUGLEVEL > 0) {
				sprintf(buf, "Send to Cash %d bytes\n", bytes_send);
				saveToFile(buf);
			}

			bzero(&mass[0], sizeof(mass));

			if (ID != 0) {
				if (err == 0) {

					while (1) {
						bytes_read = recv(sd_out, &mass[0], 2, MSG_PEEK);
						if (bytes_read > 0)
							break;
					}
					if (bytes_read > 0) {
						bytes_read = recv(sd_out, &mass[0], 2, MSG_WAITALL);
						if (bytes_read > 0) {

							ssize = mass[1];
							bytes_read += recv(sd_out, &mass[2], ssize - 2,
									MSG_WAITALL);
							if (DEBUGLEVEL > 0) {
								sprintf(buf, "Readed from Cash %d bytes\n",
										bytes_read);
								saveToFile(buf);
							}
						}
					}
				}
			}
		} else {
			if (DEBUGLEVEL > 0) {
				sprintf(buf, "Error Connection to Send: %d\n", err);
				saveToFile(buf);
			}
		}
	} else {
		if (DEBUGLEVEL > 0) {
			sprintf(buf, "Socket Error: %d\n", sd_out);
			saveToFile(buf);
		}
		bzero(&mass[0], sizeof(mass));
		err = 400;
	}

	close(sd_out);
	if (DEBUGLEVEL > 0) {
		sprintf(buf, "Close Connection\n");
		saveToFile(buf);
	}
	if (ID != 0) {
		if (ssize < 3)
			return 100;
		if (mass[0] != 2)
			return 101;
		if (ssize != mass[1])
			return 102;
		if (bytes_read != mass[1])
			return 103;
		if (mass[ssize - 1] != 3)
			return 104;
	}
	return err;

}

int addLong(long longValue, char* strDest) {
	char tmpStr[30];
	//ltoa(longValue, tmpStr, 10);
	sprintf(tmpStr, "%ld", longValue);
	strcat(strDest, tmpStr);
	return 0;
}

int addString(const char* strValue, char* strDest) {
	if (strValue == NULL)
		strcat(strDest, "NULL");
	else {
	     	if((strlen(strValue)<2)||(strlen(strValue)> 100)){strcat(strDest, "NULL");}
	     	else strncat(strDest, strValue, strlen(strValue));
	}
	return 0;
}

int addnString(char* strValue, char* strDest) {
	if (strValue == NULL)
		strcat(strDest, "NULL");
	else {
	     	if((strlen(strValue)<2)||(strlen(strValue)> 100)){strcat(strDest, "NULL");}
	     	else strncat(strDest, strValue, strlen(strValue));
	}
	return 0;
}



char* strvvv(char *qs, char *param){
    char *tok, *val, *par, *str, *str0 = NULL, *ret = NULL;
 //   if(!qs && !get_qs()) return NULL;
    str = str0 = strdup(qs);
    tok = strtok(str, "& ;");

    do{
            if(strcasecmp(tok, param)==0){
                    ret = strdup(" "); // переменная есть, значения нет
                    break;
            }
            if((val = strchr(tok, '=')) == NULL) continue;
            *val++ = '\0';
            par = tok;
            if(strcasecmp(par, param)==0){
                    if(strlen(val) > 0)
                            ret = strdup(val);
                    else
                            ret = strdup(" "); // переменная есть, значения нет
                    break;
            }
    }while((tok = strtok(NULL, "& ;"))!=NULL);

    printf("HHH:%s\n",ret);
//    free(str0);
    return ret;
}

// ------------------------------------------------------------------
//
// Display	Shows text strings.
//
// Returns:
// ------------------------------------------------------------------
int ScreenShow(ScreenParams* pScreenParams) {
	int err;
	char mass[1000];
	char massToBank[1000];
	char buffer[BUFFER_SIZE];
	char *buf = &buffer[0];
	bzero(&ipAddress[0], sizeof(ipAddress));

	if (loadConfig(&ipAddress[0], &ipPortNum) != 0) {
		strcpy(&ipAddress[0], "127.0.0.1");
		ipPortNum = 6517;
	}
	if (DEBUGLEVEL > 0) {
		saveToFile("\nVersion DLL : 1.004\n");
		sprintf(buf, "IPAddr:%s\nIPPort:%d\n", ipAddress, ipPortNum);
		saveToFile(buf);
	}

	if (pScreenParams) {
		if (DEBUGLEVEL > 0)saveToFile("pScreenParams is not NULL\n");
		long status = 0;

		bzero(&mass[0], sizeof(mass));
		bzero(&massToBank[0], sizeof(massToBank));
		strcpy(mass, "Status=");
		addLong(status, mass);
		addString(";len=", mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS01:");saveToFile(mass);}
		addLong(pScreenParams->len, mass);
		addString(";screenID=", mass);
		addLong(pScreenParams->screenID, mass);
		addString(";maxInp=", mass);
		addLong(pScreenParams->maxInp, mass);
		addString(";minInp=", mass);
		addLong(pScreenParams->minInp, mass);
		addString(";format=", mass);
		addLong(pScreenParams->format, mass);
		addString(";pTitle=", mass);
		addString(pScreenParams->pTitle, mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS02:");saveToFile(mass);}
		addString(";pStr1=", mass);
		addString(pScreenParams->pStr[0], mass);
		addString(";pStr2=", mass);
		addString(pScreenParams->pStr[1], mass);
		addString(";pStr3=", mass);
		addString(pScreenParams->pStr[2], mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS05:");saveToFile(mass);}
		addString(";pStr4=", mass);
		addString(pScreenParams->pStr[3], mass);
		addString(";pStr5=", mass);
		addString(pScreenParams->pStr[4], mass);
		addString(";pStr6=", mass);
		addString(pScreenParams->pStr[5], mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS08:");saveToFile(mass);}
		addString(";pStr7=", mass);
		addString(pScreenParams->pStr[6], mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS09:");saveToFile(mass);}
		addString(";pStr8=", mass);
		addString(pScreenParams->pStr[7], mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS10:");saveToFile(mass);}
		addString(";pStr9=", mass);
		addString(pScreenParams->pStr[8], mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS11:");saveToFile(mass);}
		addString(";pStr10=", mass);
		addString(pScreenParams->pStr[9], mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS12:");saveToFile(mass);}
		addString(";pInitStr=", mass);
		addString(pScreenParams->pInitStr, mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS13:");saveToFile(mass);}
		addString(";pButton0=", mass);
		addString(pScreenParams->pButton0, mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS14:");saveToFile(mass);}
		addString(";pButton1=", mass);
		addString(pScreenParams->pButton1, mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS15:");saveToFile(mass);}

		addString(";CurAlpha=", mass);
		addString(pScreenParams->CurParam.CurAlpha, mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS16:");saveToFile(mass);}
		addString(";nDecPoint=", mass);
		addLong(pScreenParams->CurParam.nDecPoint, mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS17:");saveToFile(mass);}
		addString(";eventKey=", mass);
		addLong(pScreenParams->eventKey, mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS18:");saveToFile(mass);}
		addString(";pBuf=", mass);
		addnString(pScreenParams->pBuf, mass);
		if (DEBUGLEVEL > 0){saveToFile("\nS19:");saveToFile(mass);}

		err = connectToCash(ipAddress, ipPortNum, mass,	pScreenParams->screenID);

		if (DEBUGLEVEL > 0) {
			sprintf(buf, "Connect to cash finished: %d\n", err);
			saveToFile(buf);
		}
		if (pScreenParams->screenID != 0) {
			if (err == 0) {
				int lll = mass[1];
				printf("Length: %d\n", mass[1]);

				strncpy(&massToBank[0], &mass[2], lll - 3);

				char *qs = &massToBank[0];
				char *spBuf,*sEventKey;
				int eventKey;
				sEventKey=strvvv(qs,"eventKey");
				spBuf=strvvv(qs,"pBuf");


				if(sEventKey != NULL)
				{
				    saveToFile("eventKey is PreSaved\n");
					sscanf(sEventKey,"%d",&eventKey);
				    pScreenParams->eventKey=eventKey;
				    saveToFile("eventKey is Saved\n");
				}
				if(spBuf != NULL)
				{
					if(strcmp(spBuf,"NULL") == 0){
						saveToFile("pBuf is NULL\n");

					}
					else{
			        saveToFile("pBuf is PreSaved\n");
				    strcpy(pScreenParams->pBuf, spBuf);
			        saveToFile("pBuf is Saved\n");
					}
				}

				if (DEBUGLEVEL > 0){
					printf("eventKey = %s = %d\n",sEventKey, eventKey);
					printf("pBuf = %s\n",spBuf);
					saveToFile(massToBank);
				}


			}
		}
	} else {
		err = 300;
	}

	if (DEBUGLEVEL > 0) {
		sprintf(buf, "\nFunction ScreenShow is closed: %d\n", err);
		saveToFile(buf);
	}

	return err;
}

void ScreenClose() {
	if (DEBUGLEVEL > 0) {
		saveToFile("ScreenClose\n");
	}
	return;
}

